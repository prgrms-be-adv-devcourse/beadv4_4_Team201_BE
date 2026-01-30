
package app.giftify.settlement.domain.exception;

import app.giftify.settlement.domain.errorCode.ErrorCode;

// "시스템이 일을 못한 것"
public class InfraException extends SettlementException {
    public InfraException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InfraException(ErrorCode errorCode, Throwable cause) {
        super(errorCode);
        initCause(cause);
    }
}