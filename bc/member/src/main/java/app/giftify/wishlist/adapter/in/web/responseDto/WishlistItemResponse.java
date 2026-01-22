package app.giftify.wishlist.adapter.in.web.responseDto;

import java.time.LocalDate;

import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import lombok.Builder;

@Builder
public record WishlistItemResponse(
	Long id,
	Long wishlistId,
	Long productId,
	WishlistItemStatus status,
	LocalDate addedAt
) {
	public static WishlistItemResponse from(WishlistItem item) {
		return WishlistItemResponse.builder()
			.id(item.getId())
			// .authSub(item.getAuthSub())
			.wishlistId(item.getWishlistId())
			.productId(item.getProductId())
			.status(item.getWishlistItemStatus())
			.addedAt(item.getAddedAt())
			.build();
	}
}
