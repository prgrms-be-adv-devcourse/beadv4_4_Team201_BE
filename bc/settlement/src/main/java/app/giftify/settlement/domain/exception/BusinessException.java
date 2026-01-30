package app.giftify.settlement.domain.exception;

import app.giftify.settlement.domain.errorCode.ErrorCode;

// "의도된 실패"의 최상위
public abstract class BusinessException extends SettlementException {
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}