package app.giftify.shared.api;

import java.math.BigDecimal;

public record AmountSummaryProjection (
        Long orderId,
        BigDecimal totalAmount
) {
}