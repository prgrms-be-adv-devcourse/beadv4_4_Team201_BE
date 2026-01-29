package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethodType;

import java.time.LocalDateTime;

public record PaymentCompleteEvent(
        Long orderId,
        Long paymentId,
        String paymentKey,
        String transactionKey,
        PaymentMethodType method,
        LocalDateTime paidAt
) {
}