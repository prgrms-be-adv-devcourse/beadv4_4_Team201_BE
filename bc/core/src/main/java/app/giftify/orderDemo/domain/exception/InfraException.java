package app.giftify.orderDemo.domain.exception;

import app.giftify.orderDemo.domain.errorCode.ErrorCode;

// "시스템이 일을 못한 것"
public class InfraException extends BaseException {
    public InfraException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InfraException(ErrorCode errorCode, Throwable cause) {
        super(errorCode);
        initCause(cause);
    }
}