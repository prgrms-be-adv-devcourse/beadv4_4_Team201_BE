package app.giftify.wishlist.application.port.in;

import app.giftify.product.domain.ProductCategory;
import app.giftify.wishlist.core.domain.WishlistItem;

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
