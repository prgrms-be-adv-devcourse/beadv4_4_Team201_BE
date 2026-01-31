package app.giftify.funding.adpater.inbound.dto;

public record WishlistItemDto(
    Long wishlistItemId,
    Long receiverId,
    Long productId,
    String productName,
    Integer productPrice
) {
}
