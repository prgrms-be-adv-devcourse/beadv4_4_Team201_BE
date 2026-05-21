package app.giftify.funding.adapter.inbound.dto;

public record WishlistItemDto(
    Long wishlistItemId,
    Long receiverId,
    String productName,
    Integer productPrice
) {
}
