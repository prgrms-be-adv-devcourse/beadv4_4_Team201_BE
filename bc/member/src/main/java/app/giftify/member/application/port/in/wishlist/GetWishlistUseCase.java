package app.giftify.member.application.port.in.wishlist;

import app.giftify.member.core.domain.wishlist.Wishlist;

import java.util.Optional;

public interface GetWishlistUseCase {
    Optional<Wishlist> getWishlistByMemberId(Long memberId);
}
