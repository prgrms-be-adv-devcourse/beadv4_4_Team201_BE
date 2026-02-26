package app.giftify.wishlist.core.domain.exception;

public class NotWishlistOwnerException extends WishlistDomainException {
    public NotWishlistOwnerException() {
        super(WishlistErrorCode.NOT_WISHLIST_OWNER);
    }
}
