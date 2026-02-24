package app.giftify.wallet.domain;

import app.giftify.shared.api.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum WalletErrorCode implements ErrorCode {
	// [100 ~ 199] 조회, 리소스 존재 여부
	WALLET_NOT_FOUND (HttpStatus.NOT_FOUND.value(), "WAL_101", "지갑을 찾을 수 없습니다."),

	// [200 ~ 299] 상태 변경 및 비즈니스 흐름 제어
	INSUFFICIENT_BALANCE (HttpStatus.BAD_REQUEST.value(), "WAL_201", "잔액이 부족합니다."),
	CHARGE_AMOUNT_BELOW_MINIMUM (HttpStatus.BAD_REQUEST.value(), "WAL_202", "최소 충전 금액은 1,000원입니다."),
	INVALID_AMOUNT (HttpStatus.BAD_REQUEST.value(), "WAL_203", "유효하지 않은 금액입니다."),

	// [300 ~ 399] 정책 및 중복 검증
	DUPLICATED_TRANSACTION (HttpStatus.BAD_REQUEST.value(), "WAL_301", "중복된 거래입니다."),

	// [400 ~ 499] PG 연동 관련
	PG_CONFIRM_FAILED (HttpStatus.BAD_GATEWAY.value(), "WAL_401", "PG 결제 승인에 실패했습니다.");

	private final int statusCode;
	private final String code;
	private final String message;

	WalletErrorCode(int statusCode, String code, String message) {
		this.statusCode = statusCode;
		this.code = code;
		this.message = message;
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public String formatMessage(Object... args) {
		return String.format(this.message, args);
	}
}
