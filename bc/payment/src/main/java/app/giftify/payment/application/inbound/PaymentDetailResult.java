package app.giftify.payment.application.inbound;

import java.time.LocalDateTime;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentDetailResult(
	Long paymentId,
	String orderNumber,
	PaymentType type,
	PaymentMethod method,
	Money originAmount,
	Money paidAmount,
	PaymentStatus status,
	LocalDateTime paidAt
) {
	public static PaymentDetailResult from(Payment payment) {
		return new PaymentDetailResult(
			payment.getId(),
			payment.getOrderNumber(),
			payment.getType(),
			payment.getMethod(),
			payment.getOriginAmount(),
			payment.getPaidAmount(),
			payment.getStatus(),
			payment.getPaidAt()
		);
	}
}
