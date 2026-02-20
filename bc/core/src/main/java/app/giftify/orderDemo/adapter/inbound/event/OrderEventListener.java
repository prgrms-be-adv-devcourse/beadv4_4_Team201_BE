package app.giftify.orderDemo.adapter.inbound.event;

import app.giftify.orderDemo.application.OrderService;
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
            retryFor = InfraException.class,
            exceptionExpression = "@retryService.isRetryable(#root)",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    public void on(PaymentCanceledEvent event) {
        log.info("[이벤트 수신] 결제 취소 완료 -> 주문 상태 변경 시작. OrderId: {}", event.getOrderId());
        orderService.completeCancel(event.getOrderId());
    }

    @Retryable(
            retryFor = InfraException.class,
            exceptionExpression = "@retryService.isRetryable(#root)",
            backoff = @Backoff(delay = 100, multiplier = 2.0, random = true)
    )
    @ApplicationModuleListener
    public void on(PaymentFailedCancelEvent event) {
        log.info("[이벤트 수신] 결제 취소 실패 -> 주문 실패 반영 시작. OrderId: {}", event.getOrderId());
        orderService.failCancel(event.getOrderId());
    }

    /**
     * 모든 재시도 실패 시 실행되는 공통 복구 로직
     */
    @Recover
    public void recover(InfraException e, Object event) {
        Long orderId = getOrderIdFromEvent(event);
        log.error("================================================================");
        log.error("[최종 장애] 주문 취소 처리 최종 실패 (시스템 자동 복구 불가)");
        log.error("OrderId: {}", orderId);
        log.error("Event Type: {}", event.getClass().getSimpleName());
        log.error("Reason: {}", e.getMessage());
        log.error("조치 사항: DB 상태 확인 후 수동 정정 필요");
        log.error("================================================================");

        throw e;
    }

    private Long getOrderIdFromEvent(Object event) {
        if (event instanceof PaymentCanceledEvent) return ((PaymentCanceledEvent) event).getOrderId();
        if (event instanceof PaymentFailedCancelEvent) return ((PaymentFailedCancelEvent) event).getOrderId();
        return null;
    }
}
