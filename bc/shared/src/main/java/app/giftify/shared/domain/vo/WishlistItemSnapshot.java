package app.giftify.shared.domain.vo;

public record WishlistItemSnapshot(
        Long originalWishlistItemId,
        Long productId,
        String productName,
        int productPrice,
        Long sellerId,
        Long wishlistOwnerId,
        String imageKey
) {
}
