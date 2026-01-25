package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentStatus;

public record PaymentCreatedResult(
	Long paymentId,
	String idempotencyKey,
	PaymentStatus status, // PENDING
	boolean requiresPgApproval //  PG 승인 필요 여부 (method가 WALLET이면 false)
) {
}
