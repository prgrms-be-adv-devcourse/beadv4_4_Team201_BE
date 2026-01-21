package app.giftify.wishlist.core.domain.exception;

public class InvalidWishlistItemStatusException extends WishlistDomainException {

    public InvalidWishlistItemStatusException(String status) {
        super(WishlistErrorCode.INVALID_WISHLIST_ITEM_STAUTS, "위시리스트 공개 범위에 대한 입력이 올바르지 않습니다. (status: " + status + ")");
    }
}
