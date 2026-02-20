package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentPaidExternalEvent(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	String orderNumber,
	Long memberId,
	Money paidAmount,
	PaymentType type,
	PaymentMethod method,
	String paymentKey,
	String transactionKey
) implements PaymentExternalEvent {

	public static PaymentPaidExternalEvent create(
		Long paymentId,
		String orderNumber,
		Long memberId,
		Money paidAmount,
		PaymentType type,
		PaymentMethod method,
		String paymentKey,
		String transactionKey,
		LocalDateTime occurredAt
	) {
		return new PaymentPaidExternalEvent(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			orderNumber,
			memberId,
			paidAmount,
			type,
			method,
			paymentKey,
			transactionKey
		);
	}
}
