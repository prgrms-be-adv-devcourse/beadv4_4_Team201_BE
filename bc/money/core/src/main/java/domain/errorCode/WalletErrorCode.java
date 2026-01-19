package domain.errorCode;

import app.giftify.shared.api.exception.ErrorCode;

public enum WalletErrorCode implements ErrorCode {
    INSUFFICIENT_BALANCE("WALLET-001", "잔액이 부족합니다."),
    INVALID_NULL_AMOUNT("WALLET-002", "금액은 null일 수 없습니다."),
    DUPLICATED_TRANSACTION("WALLET-003", "이미 처리된 거래입니다."),
    WALLET_CHARGE_AMOUNT_BELOW_MINIMUM("WALLET-004", "충전 금액은 최소 1000원이어야 합니다."),
    ;

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
