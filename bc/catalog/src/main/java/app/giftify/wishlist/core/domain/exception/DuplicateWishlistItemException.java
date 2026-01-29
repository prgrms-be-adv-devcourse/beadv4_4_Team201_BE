package app.giftify.wishlist.core.domain.exception;

public class DuplicateWishlistItemException extends WishlistDomainException {
	public DuplicateWishlistItemException(Long wishlistId, Long productId) {
		super(WishlistErrorCode.DUPLICATE_WISHLIST_ITEM,
			String.format("이미 위시리스트에 존재하는 상품입니다. (wishlistId: %s, productId: %d)", wishlistId, productId));
	}
}
