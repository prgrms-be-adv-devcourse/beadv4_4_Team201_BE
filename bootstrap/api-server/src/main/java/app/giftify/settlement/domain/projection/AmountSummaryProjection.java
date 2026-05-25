package app.giftify.settlement.domain.projection;

import java.math.BigDecimal;

public record AmountSummaryProjection (
        Long orderId,
        BigDecimal totalAmount
) {
}