package app.giftify.wishlist.core.domain.exception;

public class WishlistNotAccessibleException extends WishlistDomainException {
    public WishlistNotAccessibleException() {
        super(WishlistErrorCode.WISHLIST_NOT_ACCESSIBLE);
    }
}
