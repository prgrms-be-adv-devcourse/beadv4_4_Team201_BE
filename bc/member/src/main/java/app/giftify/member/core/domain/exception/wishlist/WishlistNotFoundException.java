package app.giftify.member.core.domain.exception.wishlist;

public class WishlistNotFoundException extends WishlistDomainException {
    public WishlistNotFoundException() {
        super(WishlistErrorCode.WISHLIST_NOT_FOUND);
    }

    public WishlistNotFoundException(Long wishlistId) {
        super(WishlistErrorCode.WISHLIST_NOT_FOUND, "위시리스트를 찾을 수 없습니다. (ID: " + wishlistId + ")");
    }
}
