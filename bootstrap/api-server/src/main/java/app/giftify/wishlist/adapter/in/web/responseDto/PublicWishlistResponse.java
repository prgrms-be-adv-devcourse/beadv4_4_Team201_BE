package app.giftify.wishlist.adapter.in.web.responseDto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record PublicWishlistResponse(
	Long memberId,
	String nickname,
	List<PublicWishlistItemDto> items
) {
	@Builder
	public record PublicWishlistItemDto(
		Long wishlistItemId,
		Long productId,
		String productName,
		int price,
		LocalDateTime addedAt
	) {
	}
}
