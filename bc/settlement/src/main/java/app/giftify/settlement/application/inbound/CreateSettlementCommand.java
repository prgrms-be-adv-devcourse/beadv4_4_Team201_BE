package app.giftify.settlement.application.inbound;

import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;

public record CreateSettlementCommand(
        OrderItemSnapshot snapshot
) {
}
