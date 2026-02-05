package app.giftify.orderDemo.application.inbound.vo;

import java.time.LocalDateTime;

public record PaymentSnapshot(
        Long orderId,
        String paymentKey,
        String lastTransactionKey,
        LocalDateTime createdAt
) {
}