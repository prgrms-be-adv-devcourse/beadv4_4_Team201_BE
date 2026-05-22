package app.giftify.funding.application;

public record FundingFromOrderCommand(
        Long wishlistItemId,
        Long participantId,
        Long receiverId,
        Integer amount
) {
}
