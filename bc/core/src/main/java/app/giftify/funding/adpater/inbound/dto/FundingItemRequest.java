package app.giftify.funding.adpater.inbound.dto;

public record FundingItemRequest(
        Long wishlistItemId,
        Integer amount
) {
}
