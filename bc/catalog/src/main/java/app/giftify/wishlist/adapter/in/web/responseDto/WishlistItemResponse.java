package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemDetail;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WishlistItemResponse(
        Long id,
        Long wishlistId,
        Long productId,
        String productName,
        int price,
        String imageKey,
        boolean isSoldout,
        boolean isActive,
        String sellerNickname,
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

    public static WishlistItemResponse from(WishlistItemDetail detail) {
        WishlistItem item = detail.wishlistItem();
        return WishlistItemResponse.builder()
                .id(item.getId())
                .wishlistId(item.getWishlistId())
                .productId(item.getProductId())
                .productName(detail.productName())
                .price(detail.price())
                .imageKey(detail.imageKey())
                .isSoldout(detail.isSoldout())
                .isActive(detail.isActive())
                .sellerNickname(detail.sellerNickname())
                .status(item.getWishlistItemStatus())
                .addedAt(item.getAddedAt())
                .build();
    }
}
