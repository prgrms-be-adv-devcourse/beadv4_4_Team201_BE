package app.giftify.wishlist.application.port.in;

import java.util.Optional;

import app.giftify.wishlist.core.domain.Wishlist;

public interface GetWishlistUseCase {
	// Optional<Wishlist> getWishlistByAuthSub(String authSub);

	Optional<Wishlist> getWishlistByMemberId(Long memberId);
}
