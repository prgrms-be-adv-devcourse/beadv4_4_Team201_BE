package app.giftify.settlement.domain.exception;

import app.giftify.settlement.domain.errorCode.ErrorCode;

public class DomainException extends BusinessException {
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }
}
