package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItemDetail;

import java.util.List;

public interface GetWishlistUseCase {
    Wishlist getOrCreateWishlistByMemberId(Long memberId);

    List<WishlistItemDetail> getWishlistItemDetails(Long targetMemberId, Long currentMemberId);
}
