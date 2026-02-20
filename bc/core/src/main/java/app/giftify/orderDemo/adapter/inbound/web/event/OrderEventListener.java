package app.giftify.orderDemo.adapter.inbound.web.event;

import app.giftify.orderDemo.application.OrderService;
import app.giftify.shared.api.exception.BusinessException;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedCancelEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderService orderService;

    @Retryable(
            retryFor = {InfraException.class},
            exceptionExpression = "@retryService.isRetryable(#root)",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    void on(PaymentCanceledEvent event) {
        Long orderId = event.getOrderId();

        executeWithRetry(() -> orderService.completeCancel(orderId), orderId);
    }

    @Recover
    void recover(InfraException e, PaymentCanceledEvent event) {
        log.error("주문 취소 확정 최종 실패. 결제 취소 건과 데이터 대조 필요. OrderId: {}", event.getOrderId());
    }

    @Retryable(
            retryFor = {InfraException.class},
            exceptionExpression = "@retryService.isRetryable(#root)",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    void on(PaymentFailedCancelEvent event) {
        Long orderId = event.getOrderId();

        executeWithRetry(() -> orderService.failCancel(orderId), orderId);
    }

    @Recover
    void recover(InfraException e, PaymentFailedCancelEvent event) {
        log.error("주문 취소 실패 반영 실패. 결제 취소 건과 데이터 대조 필요. OrderId: {}", event.getOrderId());
    }

    private void executeWithRetry(Runnable action, Long orderId) {
        try {
            action.run();
        } catch (BusinessException e) {
            log.warn("비즈니스 예외 발생 - OrderId: {}, Error: {}", orderId, e.getErrorCode());
        } catch (InfraException e) {
            log.info("인프라 예외 발생. 재시도 여부 판단 단계로 진입합니다. errorCode: {}", e.getErrorCode());
            throw e;
        }
    }
}
