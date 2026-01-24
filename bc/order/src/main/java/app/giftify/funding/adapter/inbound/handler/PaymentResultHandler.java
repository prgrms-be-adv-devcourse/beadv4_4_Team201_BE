package app.giftify.funding.adapter.inbound.handler;

import app.giftify.funding.application.inbound.PaymentResultUseCase;
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
        // 이벤트 필수 값 검증
        if (event.getOrderId() == null || event.getPaymentKey() == null) {
            log.error("[PaymentResultHandler] 결제 성공 이벤트 검증 실패: orderId 또는 paymentKey 누락");
            return;
        }

        paymentResultUseCase.completePayment(event.getOrderId(), event.getPaymentKey());
    }
}
