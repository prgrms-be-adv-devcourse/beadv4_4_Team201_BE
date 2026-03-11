package app.giftify.order.application.inbound.command;

public record ConfirmFundingOrderCommand(
        Long fundingId,
        Long productId
) {
}
