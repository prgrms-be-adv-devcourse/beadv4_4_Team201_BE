package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public record PaymentCompleteEvent(
        Long orderId,
        Long paymentId,
        String paymentKey,
        String transactionKey,
        Money paidAmount,
        PaymentMethodType method,
        LocalDateTime paidAt
) {
}