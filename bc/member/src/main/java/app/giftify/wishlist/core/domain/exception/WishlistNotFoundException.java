package app.giftify.wishlist.core.domain.exception;

public class WishlistNotFoundException extends WishlistDomainException {
	public WishlistNotFoundException() {
		super(WishlistErrorCode.WISHLIST_NOT_FOUND);
	}

	public WishlistNotFoundException(Long memberId) {
		super(WishlistErrorCode.WISHLIST_NOT_FOUND, "위시리스트를 찾을 수 없습니다. (Member ID: " + memberId + ")");
	}
}
