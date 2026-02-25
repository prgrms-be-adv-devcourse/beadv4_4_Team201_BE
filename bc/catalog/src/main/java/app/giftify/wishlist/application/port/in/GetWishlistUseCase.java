package app.giftify.wishlist.application.port.in;

import app.giftify.shared.api.paging.Page;
import app.giftify.shared.api.paging.PageRequest;
import app.giftify.wishlist.core.domain.Wishlist;

import java.util.List;

public interface GetWishlistUseCase {
    Wishlist getOrCreateWishlistByMemberId(Long memberId);

    Page<WishlistItemDetail> getMyWishlistItemDetails(Long memberId, PageRequest pageRequest);

    List<WishlistItemDetail> getMyWishlistItemDetails(Long memberId);

    List<WishlistItemDetail> getWishlistItemDetails(Long targetMemberId, Long currentMemberId);

    Page<WishlistItemDetail> getWishlistItemDetails(Long targetMemberId, Long currentMemberId, PageRequest pageRequest);

    WishlistOverview getMyWishlistOverview(Long memberId, PageRequest pageRequest);

    WishlistOverview getWishlistOverview(Long targetMemberId, Long currentMemberId, PageRequest pageRequest);
}
