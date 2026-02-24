package app.giftify.wishlist.application.port.in;

import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItemDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GetWishlistUseCase {
    Wishlist getOrCreateWishlistByMemberId(Long memberId);

    Page<WishlistItemDetail> getMyWishlistItemDetails(Long memberId, Pageable pageable);

    List<WishlistItemDetail> getMyWishlistItemDetails(Long memberId);

    List<WishlistItemDetail> getWishlistItemDetails(Long targetMemberId, Long currentMemberId);
}
