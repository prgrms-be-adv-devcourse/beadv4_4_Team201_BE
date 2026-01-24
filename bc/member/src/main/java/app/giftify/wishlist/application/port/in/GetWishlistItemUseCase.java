package app.giftify.wishlist.application.port.in;

import java.util.List;

import app.giftify.wishlist.core.domain.WishlistItem;

public interface GetWishlistItemUseCase {
	// 위시리스트에 존재하는 아이템이 존재하는지 확인
	Long getWishlistItemCount();

	// 위시리스트에 이미 존재하는 아이템인지 확인
	boolean isItemExists(Long memberId, Long productId);

	List<WishlistItem> getWishlistItems(Long wishlistId);
}
