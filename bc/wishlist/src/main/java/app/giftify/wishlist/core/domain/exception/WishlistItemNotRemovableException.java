package app.giftify.wishlist.core.domain.exception;

import app.giftify.wishlist.core.domain.WishlistItemStatus;

public class WishlistItemNotRemovableException extends WishlistDomainException {
	public WishlistItemNotRemovableException(WishlistItemStatus status) {
		super(WishlistErrorCode.WISHLIST_ITEM_NOT_REMOVABLE, "위시리스트 아이템을 삭제할 수 있는 상태가 아닙니다. 현재 상태: " + status);
	}
}
