package app.giftify.orderDemo.domain.exception;

import app.giftify.orderDemo.domain.errorCode.ErrorCode;

public class PolicyException extends BusinessException {
    public PolicyException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PolicyException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public PolicyException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}