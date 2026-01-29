package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WishlistItemResponse(
        Long id,
        Long wishlistId,
        Long productId,
        WishlistItemStatus status,
        LocalDateTime addedAt
) {
    public static WishlistItemResponse from(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .wishlistId(item.getWishlistId())
                .productId(item.getProductId())
                .status(item.getWishlistItemStatus())
                .addedAt(item.getAddedAt())
                .build();
    }
}
