package app.giftify.settlement.application.inbound;

public record CancelSettlementCommand(
        Long orderId,
        Long orderItemId
) {
}
