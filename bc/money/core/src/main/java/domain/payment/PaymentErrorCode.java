package domain.payment;

import app.giftify.shared.api.exception.ErrorCode;

public enum PaymentErrorCode implements ErrorCode {
	// [000 ~ 099] 공통 및 입력값 유효성
	INVALID_INPUT_VALUE("PAY_001", "유효하지 않은 입력값입니다."),

	// [100 ~ 199] 조회 및 리소스 존재 여부
	PAYMENT_NOT_FOUND("PAY_101", "결제 내역을 찾을 수 없습니다."),

	// [200 ~ 299] 상태 변경 및 비즈니스 흐름 제어
	INVALID_PAYMENT_STATUS("PAY_201", "유효하지 않은 결제 상태 변경입니다."),
	NOT_REFUNDABLE("PAY_202", "환불 불가능한 상태입니다."),
	NOT_CANCELABLE("PAY_203", "취소 불가능한 상태입니다."),
	NOT_SETTLEABLE("PAY_204", "정산 불가능한 상태입니다."),
	ALREADY_PAID("PAY_205", "이미 결제 완료된 건입니다."),
	ALREADY_CANCELED("PAY_206", "이미 취소된 결제입니다."), // 추가 제안

	// [300 ~ 399] 정책 및 타입 검증
	UNSUPPORTED_PAYMENT_TYPE("PAY_301", "지원하지 않는 결제 타입입니다."),
	AMOUNT_MISMATCH("PAY_302", "결제 금액이 일치하지 않습니다."),
	INSUFFICIENT_WALLET_BALANCE("PAY_303", "예치금 잔액이 부족합니다."),

	// [400 ~ 499] PG 연동 관련
	PG_APPROVAL_FAILED("PAY_401", "PG사 승인이 실패했습니다."),
	PG_CONNECTION_ERROR("PAY_402", "PG사 연결 중 오류가 발생했습니다."),

	// [900 ~ 999] 시스템 및 내부 오류
	INTERNAL_SERVER_ERROR("PAY_999", "서버 내부 오류가 발생했습니다.");

	private final String code;
	private final String message;

	PaymentErrorCode(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String getCode() {
		return this.code;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
