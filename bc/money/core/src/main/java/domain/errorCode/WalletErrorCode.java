package domain.errorCode;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WalletErrorCode implements ErrorCode {
    INSUFFICIENT_BALANCE("WALLET-001", "잔액이 부족합니다."),
    INVALID_NULL_AMOUNT("WALLET-002", "금액은 null일 수 없습니다.");

    private final String code;
    private final String message;
}
