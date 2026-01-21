package app.giftify.wishlist.adapter.in.web.responseDto;

import app.giftify.wishlist.core.domain.ItemStatus;
import app.giftify.wishlist.core.domain.WishlistItem;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record WishlistItemResponse(
        Long id,
        String authSub,
        Long productId,
        ItemStatus status,
        LocalDate addedAt
) {
    public static WishlistItemResponse from(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .authSub(item.getAuthSub())
                .productId(item.getProductId())
                .status(item.getItemStatus())
                .addedAt(item.getAddedAt())
                .build();
    }
}
