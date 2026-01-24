package app.giftify.funding.adapter.inbound.handler;

import app.giftify.funding.application.inbound.PaymentResultUseCase;
import app.giftify.shared.domain.event.payment.PaymentCancelledForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentRefundedForOrderEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededForOrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultHandler {

    private final PaymentResultUseCase paymentResultUseCase;

    public void handlePaymentResultSucceed(PaymentSucceededForOrderEvent event) {
        if (event.getOrderId() == null || event.getPaymentKey() == null) {
            log.error("[PaymentResultHandler] 결제 성공 이벤트 검증 실패: orderId 또는 paymentKey 누락");
            return;
        }

        paymentResultUseCase.completePayment(event.getOrderId(), event.getPaymentKey());
    }

    public void handlePaymentResultRefund(PaymentRefundedForOrderEvent event) {
        if (event.getOrderId() == null || event.getReason() == null) {
            log.error("[PaymentResultHandler] 환불 이벤트 검증 실패: orderId 또는 reason 누락");
            return;
        }

        paymentResultUseCase.refundPayment(event.getOrderId(), event.getReason());
    }

    public void handlePaymentResultFail(PaymentFailedForOrderEvent event) {
        if (event.getOrderId() == null) {
            log.error("[PaymentResultHandler] 결제 재시도용 취소 이벤트 검증 실패: orderId 누락");
            return;
        }

        paymentResultUseCase.failPayment(event.getOrderId());
    }

    public void handlePaymentResultCancel(PaymentCancelledForOrderEvent event) {
        if (event.getOrderId() == null) {
            log.error("[PaymentResultHandler] 결제 취소 이벤트 검증 실패: orderId 누락");
            return;
        }

        paymentResultUseCase.cancelPayment(event.getOrderId());
    }
}
