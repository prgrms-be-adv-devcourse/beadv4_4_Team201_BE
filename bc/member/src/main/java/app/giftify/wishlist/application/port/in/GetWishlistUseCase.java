package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.Wishlist;

import java.util.Optional;

public interface GetWishlistUseCase {
    Optional<Wishlist> getWishlistByAuthSub(String authSub);
}
