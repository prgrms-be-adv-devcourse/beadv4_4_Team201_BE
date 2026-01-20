package app.giftify.member.application.port.in.wishlist;

import app.giftify.member.core.domain.wishlist.WishlistItem;

import java.util.List;

public interface GetWishlistItemUseCase {
    // 위시리스트에 존재하는 아이템이 존재하는지 확인
    Long getWishlistItemCount(Long wishlistId);

    // 위시리스트에 이미 존재하는 아이템인지 확인
    boolean isItemExists(String authSub, Long productId);

    List<WishlistItem> getWishlistItems(String authSub);
}
