package app.giftify.payment.adapter.in.web.payment.dto;

import java.math.BigDecimal;

import domain.payment.PaymentStatus;

public record PaymentChargeResponse(
	Long paymentId,
	String orderId,
	BigDecimal amount,
	PaymentStatus status,
	String orderName
) {
	private static final String DEFAULT_ORDER_NAME = "Giftify 캐시 충전";

	public static PaymentChargeResponse of(Long paymentId, String orderId, BigDecimal amount, PaymentStatus status) {
		return new PaymentChargeResponse(paymentId, orderId, amount, status, DEFAULT_ORDER_NAME);
	}
}