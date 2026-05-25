package app.giftify.payment.adapter.inbound.web.dto;

import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.payment.domain.type.PaymentMethod;
import app.giftify.payment.domain.type.PaymentType;
import app.giftify.support.common.money.Money;

public record PaymentInfoResponse(
	Long paymentId,
	String orderNumber,
	Long memberId,
	PaymentStatus status,
	PaymentType type,
	PaymentMethod method,
	Money originAmount,
	Money paidAmount,
	String paymentKey,
	String approveCode
) {

	public static PaymentInfoResponse from(InternalPaymentResult result) {
		return new PaymentInfoResponse(
			result.paymentId(),
			result.orderNumber(),
			result.memberId(),
			result.status(),
			result.type(),
			result.method(),
			result.originAmount(),
			result.paidAmount(),
			result.paymentKey(),
			result.approveCode()
		);
	}
}
