package domain.errorCode;

import app.giftify.shared.api.exception.ErrorCode;

public enum WalletErrorCode implements ErrorCode {
    INSUFFICIENT_BALANCE("WALLET-001", "잔액이 부족합니다."),
    INVALID_NULL_AMOUNT("WALLET-002", "금액은 null일 수 없습니다.");

    private final String code;
    private final String message;

    WalletErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
