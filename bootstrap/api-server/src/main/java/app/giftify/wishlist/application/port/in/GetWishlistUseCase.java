package app.giftify.wishlist.application.port.in;

import app.giftify.support.common.api.paging.Page;
import app.giftify.support.common.api.paging.PageRequest;
import app.giftify.wishlist.core.domain.Wishlist;

public interface GetWishlistUseCase {
    Wishlist getOrCreateWishlistByMemberId(Long memberId);

    Page<WishlistItemDetail> getWishlistItemDetails(Long wishlistId, PageRequest pageRequest);

    WishlistOverview getMyWishlistOverview(Long memberId, PageRequest pageRequest);

    WishlistOverview getWishlistOverview(Long targetMemberId, Long currentMemberId, PageRequest pageRequest);
}
