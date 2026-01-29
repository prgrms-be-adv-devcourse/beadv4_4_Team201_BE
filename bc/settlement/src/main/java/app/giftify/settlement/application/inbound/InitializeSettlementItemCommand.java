package app.giftify.settlement.application.inbound;

public record InitializeSettlementItemCommand(
        Long fundingId,
        Long orderId,
        Long confirmedAt
) {
}
