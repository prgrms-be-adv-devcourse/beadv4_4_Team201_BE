package app.giftify.wishlist.adapter.in.web.responseDto;

import lombok.Builder;

@Builder
public record MemberWishlistSummaryResponse(
	Long memberId,
	String nickname
) {
}
