package app.giftify.order.domain.exception;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

public class OrderException extends DomainException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
