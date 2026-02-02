package app.giftify.orderDemo.domain.exception;

import app.giftify.orderDemo.domain.errorCode.ErrorCode;

// "의도된 실패"의 최상위
public abstract class BusinessException extends BaseException {
    protected BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}