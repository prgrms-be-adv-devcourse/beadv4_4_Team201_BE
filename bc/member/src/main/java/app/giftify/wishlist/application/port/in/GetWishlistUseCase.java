package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.Wishlist;

public interface GetWishlistUseCase {
	Wishlist getOrCreateWishlistByMemberId(Long memberId);
}
