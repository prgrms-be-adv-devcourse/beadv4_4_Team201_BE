package app.giftify.payment.adapter.in.web.payment.dto;

import domain.payment.PaymentStatus;

public record PaymentConfirmResponse(
	Long paymentId,
	PaymentStatus status
) {
}