package app.giftify.payment.domain;

public enum PaymentStatus {
	PENDING,    // 결제 대기
	PAID,       // 결제 완료
	CANCELED,   // 결제 취소
	FAILED,     // 결제 실패 -> 재시도 필요
	RECEIVED,   // 수령 확정 (환불 불가)
	REFUNDED;   // 환불 완료
}
