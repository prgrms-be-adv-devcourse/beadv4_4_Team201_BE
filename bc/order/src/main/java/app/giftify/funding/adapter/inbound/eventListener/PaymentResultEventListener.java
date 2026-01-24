package app.giftify.funding.adapter.inbound.eventListener;

import app.giftify.funding.adapter.inbound.handler.PaymentResultHandler;
import app.giftify.shared.domain.event.payment.PaymentRefundedEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededForOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// Kafka를 사용하지 않기 때문에, 컨슈머 대신 이벤트 리스너로 정의

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultEventListener {

    private final PaymentResultHandler paymentResultHandler;

    @EventListener
    public void handlePaymentResultSucceededEvent(PaymentSucceededForOrderEvent event) {
        log.info("[PaymentResultEventListener] 결제 성공 이벤트 수신: {}", event);
        paymentResultHandler.handlePaymentResultSucceed(event);
        log.info("[PaymentResultEventListener] 결제 성공 이벤트 처리 완료 ");
    }

    @EventListener
    public void handlePaymentResultRefundedEvent(PaymentRefundedForOrderEvent event) {
        log.info("[PaymentResultEventListener] 환불 이벤트 수신: {}", event);
        paymentResultHandler.handlePaymentResultRefund(event);
        log.info("[PaymentResultEventListener] 환불 이벤트 처리 완료 - 사유: {}", event.getReason());
    }
}
