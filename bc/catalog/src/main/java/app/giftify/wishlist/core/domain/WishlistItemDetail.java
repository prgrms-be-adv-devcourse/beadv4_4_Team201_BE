package app.giftify.wishlist.core.domain;

import app.giftify.product.domain.ProductCategory;

public record WishlistItemDetail(
        WishlistItem wishlistItem,
        String productName,
        int price,
        String imageKey,
        boolean isSoldout,
        boolean isActive,
        String sellerNickname,
        ProductCategory category
) {
}
