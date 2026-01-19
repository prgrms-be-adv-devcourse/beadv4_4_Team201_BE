package domain.exception;

import app.giftify.shared.api.exception.ErrorCode;

public class WalletException extends RuntimeException {

    private final ErrorCode errorCode;

    public WalletException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public WalletException(String message) {
        super(message);
        this.errorCode = null;
    }

    public WalletException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

