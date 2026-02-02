package app.giftify.payment.adapter.inbound.web.dto;

import java.math.BigDecimal;

import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.shared.domain.vo.Money;

public record PaymentChargeResponse(
	Long paymentId,
	String orderId,
	BigDecimal amount,              // 프론트엔드 Toss SDK에서 필요 !!
	String idempotencyKey,
	String status
) {
	public static PaymentChargeResponse from(PaymentCreatedResult result, Money paidAmount) {
		return new PaymentChargeResponse(
			result.paymentId(),
			result.orderId(),
			paidAmount.amount(),
			result.idempotencyKey(),
			result.status().name()
		);
	}
}
