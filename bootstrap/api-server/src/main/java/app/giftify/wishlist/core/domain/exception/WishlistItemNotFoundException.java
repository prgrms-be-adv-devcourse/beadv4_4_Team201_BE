package app.giftify.wishlist.core.domain.exception;

public class WishlistItemNotFoundException extends WishlistDomainException {
    public WishlistItemNotFoundException() {
        super(WishlistErrorCode.WISHLIST_ITEM_NOT_FOUND);
    }
}
