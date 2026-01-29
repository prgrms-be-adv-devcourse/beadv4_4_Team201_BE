package app.giftify.funding.in.funding;

public record WishlistItemDto(
    Long wishlistItemId,
    Long receiverId,
    Long productId,
    String productName,
    Integer productPrice
) {
}
