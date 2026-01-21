package app.giftify.wishlist.core.domain.exception;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

public abstract class WishlistDomainException extends DomainException {

    protected WishlistDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected WishlistDomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
