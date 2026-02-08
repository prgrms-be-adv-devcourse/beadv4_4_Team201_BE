package app.giftify.wishlist.application.port.in;

import java.util.List;

import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;

public interface GetPublicWishlistUseCase {

	// PUBLIC 위시리스트를 보유한 사용자들의 wishlistId 목록
	List<Wishlist> findPublicWishlists(List<Long> memberIds);

	// 특정 사용자의 PUBLIC 위시리스트 아이템 목록
	List<WishlistItem> getPublicWishlistItems(Long memberId);

}
