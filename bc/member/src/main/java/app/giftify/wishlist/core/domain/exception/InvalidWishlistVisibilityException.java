package app.giftify.wishlist.core.domain.exception;

public class InvalidWishlistVisibilityException extends WishlistDomainException {

    public InvalidWishlistVisibilityException(String value) {
        super(WishlistErrorCode.INVALID_WISHLIST_VALUE, "위시리스트 공개 범위에 대한 입력이 올바르지 않습니다. (Input: " + value + ")");
    }
}
