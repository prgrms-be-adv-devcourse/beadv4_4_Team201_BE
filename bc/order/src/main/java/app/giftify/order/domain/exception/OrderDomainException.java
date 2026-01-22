package app.giftify.order.domain.exception;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

public class OrderDomainException extends DomainException {

    protected OrderDomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected OrderDomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
