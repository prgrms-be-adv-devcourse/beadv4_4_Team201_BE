package app.giftify.payment.adapter.inbound.web.dto;

import app.giftify.payment.application.inbound.PaymentCreatedResult;

public record PaymentChargeResponse(
	Long paymentId,
	String orderId,
	String idempotencyKey,
	String status
) {
	public static PaymentChargeResponse from(PaymentCreatedResult result) {
		return new PaymentChargeResponse(
			result.paymentId(),
			null,
			result.idempotencyKey(),
			result.status().name()
		);
	}
}
