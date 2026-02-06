package app.giftify.orderDemo.application.inbound.vo;

import java.time.LocalDateTime;

public record MarkOrderAsPaidCommand(
        String orderNumber,
        String paymentKey,
        String lastTransactionKey,
        LocalDateTime createdAt
) {
}