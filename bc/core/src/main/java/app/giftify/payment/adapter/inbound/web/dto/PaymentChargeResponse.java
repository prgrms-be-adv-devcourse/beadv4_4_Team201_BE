package app.giftify.payment.adapter.inbound.web.dto;

import java.math.BigDecimal;

import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.shared.domain.vo.Money;

/**
 * 포인트 충전 응답 DTO.
 *
 * <p>{@code orderId}는 Toss SDK 호출 시 필요하며, 멱등성 키 역할도 겸합니다.</p>
 */
public record PaymentChargeResponse(
	Long paymentId,
	String orderId,             // Toss SDK 필수 + 멱등성 키
	BigDecimal amount,
	String status
) {
	public static PaymentChargeResponse from(PaymentCreatedResult result, Money paidAmount) {
		return new PaymentChargeResponse(
			result.paymentId(),
			result.orderId(),
			paidAmount.amount(),
			result.status().name()
		);
	}
}
