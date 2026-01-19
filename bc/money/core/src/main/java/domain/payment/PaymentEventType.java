package domain.payment;

public enum PaymentEventType {
	CREATED,
	PAID, 		// PENDING → PAID (결제 완료)
	FAILED, 	// - PENDING → FAILED (결제 실패)
	CANCELED, 	// - PENDING → CANCELED (결제 취소)
	REFUNDED, 	// - PAID → REFUNDED (환불)
	SETTLED		// - PAID → SETTLED (확정, 환불 불가)

}
