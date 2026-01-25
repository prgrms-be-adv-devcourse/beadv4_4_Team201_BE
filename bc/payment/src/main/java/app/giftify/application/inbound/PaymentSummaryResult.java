package app.giftify.application.inbound;

import java.time.LocalDateTime;

import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.vo.Money;

public record PaymentSummaryResult(
	Long paymentId,
	String orderId,
	Money paidAmount,
	PaymentStatus status,
	LocalDateTime createdAt
) {
}
