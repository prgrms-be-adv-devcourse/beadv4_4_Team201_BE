package app.giftify.cart.core.domain.exception;

import app.giftify.support.common.api.exception.DomainException;
import app.giftify.support.common.api.exception.ErrorCode;

public class CartException extends DomainException {
    public CartException(ErrorCode errorCode) {super(errorCode);}

    public CartException(ErrorCode errorCode, String message) {super(errorCode, message);}

    public CartException(ErrorCode errorCode, Object... args) {
        super(errorCode, String.format(errorCode.getMessage(), args));
    }
}