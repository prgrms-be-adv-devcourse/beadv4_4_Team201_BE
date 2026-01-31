package app.giftify.orderDemo.domain.exception;

import app.giftify.orderDemo.domain.errorCode.ErrorCode;

public class DomainException extends BusinessException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }
}
