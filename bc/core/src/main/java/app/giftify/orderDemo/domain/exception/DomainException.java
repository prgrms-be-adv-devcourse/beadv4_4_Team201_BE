package app.giftify.orderDemo.domain.exception;

import app.giftify.orderDemo.domain.errorCode.ErrorCode;

public class DomainException extends BusinessException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DomainException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
