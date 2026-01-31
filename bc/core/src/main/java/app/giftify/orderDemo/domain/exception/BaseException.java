package app.giftify.orderDemo.domain.exception;

import app.giftify.orderDemo.domain.errorCode.ErrorCode;

public abstract class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return errorCode.isRetryable();
    }
}