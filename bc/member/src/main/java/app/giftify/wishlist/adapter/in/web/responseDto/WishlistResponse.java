package app.giftify.wishlist.adapter.in.web.responseDto;

import java.time.LocalDateTime;

import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.Builder;

@Builder
public record WishlistResponse(
	Long id,
	Long memberId,
	Visibility visibility,
	LocalDateTime createdAt
) {
	public static WishlistResponse from(Wishlist wishlist) {
		return WishlistResponse.builder()
			.id(wishlist.getId())
			.memberId(wishlist.getMemberId())
			.visibility(wishlist.getVisibility())
			.createdAt(wishlist.getCreatedAt())
			.build();
	}
}
