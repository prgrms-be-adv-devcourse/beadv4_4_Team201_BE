package app.giftify.funding.adpater.inbound.dto;

public record WishlistItemDto(
    Long wishlistItemId,
    Long receiverId,
    String productName,
    Integer productPrice
) {
}
