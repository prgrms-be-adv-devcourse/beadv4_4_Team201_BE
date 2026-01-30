
package app.giftify.settlement.domain.exception;

import app.giftify.settlement.domain.errorCode.ErrorCode;

public class PolicyException extends BusinessException {
    public PolicyException(ErrorCode errorCode) {
        super(errorCode);
    }
}