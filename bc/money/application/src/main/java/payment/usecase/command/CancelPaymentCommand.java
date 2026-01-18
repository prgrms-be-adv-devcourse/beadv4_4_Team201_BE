package payment.usecase.command;

import domain.payment.CancelReason;

public record CancelPaymentCommand(
	Long paymentId,
	Long requesterId,
	CancelReason reason,
	String metadata
) {
	// 하위 호환성을 위한 생성자 (기존 코드 깨짐 방지용)
	public CancelPaymentCommand(Long paymentId, Long requesterId, CancelReason reason) {
		this(paymentId, requesterId, reason, null);
	}
}
