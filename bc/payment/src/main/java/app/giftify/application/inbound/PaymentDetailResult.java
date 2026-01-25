package app.giftify.application.inbound;

import java.time.LocalDateTime;

import app.giftify.payment.domain.PaymentMethod;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentDetailResult(
	Long paymentId,
	String orderId,
	PaymentType type,
	PaymentMethod method,
	Money originAmount,
	Money paidAmount,
	PaymentStatus status,
	LocalDateTime paidAt
) {
}
