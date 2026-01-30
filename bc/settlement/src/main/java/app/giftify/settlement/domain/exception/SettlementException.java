package app.giftify.settlement.domain.exception;

import app.giftify.settlement.domain.errorCode.ErrorCode;

public abstract class SettlementException extends BaseException{
    protected SettlementException(ErrorCode errorCode) {
        super(errorCode);
    }
}
