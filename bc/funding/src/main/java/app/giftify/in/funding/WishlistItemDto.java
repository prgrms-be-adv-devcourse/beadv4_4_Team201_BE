package app.giftify.in.funding;

public record WishlistItemDto(
    Long wishlistItemId,
    Long productId,
    String productName,
    Integer productPrice,
    Long receiverId
) {
}

