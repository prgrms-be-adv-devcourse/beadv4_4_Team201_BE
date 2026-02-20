package app.giftify.orderDemo.application.inbound.command;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MarkOrderAsPaidCommand(
        @NotNull
        String orderNumber,
        @NotNull
        Long paymentId,
        @NotNull
        String lastTransactionKey,
        @NotNull
        LocalDateTime createdAt
) {
}