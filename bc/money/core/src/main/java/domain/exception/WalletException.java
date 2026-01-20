package domain.exception;

import app.giftify.shared.api.exception.DomainException;
import app.giftify.shared.api.exception.ErrorCode;

public class WalletException extends DomainException {
    public WalletException(ErrorCode errorCode) {
        super(errorCode);
    }

    public WalletException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
