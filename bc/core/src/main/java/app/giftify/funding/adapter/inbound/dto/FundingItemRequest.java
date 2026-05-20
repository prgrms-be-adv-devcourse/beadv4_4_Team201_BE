package app.giftify.funding.adapter.inbound.dto;

public record FundingItemRequest(
        Long wishlistItemId,
        Integer amount
) {
}
