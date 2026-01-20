package domain.payment;

public enum CancelReason {
	USER_REQUEST,            // 사용자 요청
	INSUFFICIENT_FUNDS,     // 잔액 부족
	TIMEOUT,                // 결제 시간 초과
	SYSTEM_ERROR,           // 시스템 오류
	FUNDING_FAILED,         // 펀딩 실패
	PG_CANCELED,
	UNDEFINED,
}
