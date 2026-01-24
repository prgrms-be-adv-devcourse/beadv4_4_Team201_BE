package app.giftify.wishlist.core.domain.exception;

public class WishlistItemInvalidStatusTransitionException extends WishlistDomainException {
	public WishlistItemInvalidStatusTransitionException() {
		super(WishlistErrorCode.WISHLIST_ITEM_INVALID_STATUS_TRANSITION, "위시리스트 아이템 상태 전이가 허용되지 않습니다.");
	}
}
