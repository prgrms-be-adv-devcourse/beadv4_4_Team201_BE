package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentConfirmedForSettlement(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	String orderNumber,
	String paymentKey,
	String transactionKey,
	Money paidAmount,
	PaymentMethod method
) implements PaymentExternalEvent {

	public static PaymentConfirmedForSettlement create(
		Long paymentId,
		String orderNumber,
		String paymentKey,
		String transactionKey,
		Money paidAmount,
		PaymentMethod method,
		LocalDateTime occurredAt
	) {
		return new PaymentConfirmedForSettlement(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			orderNumber,
			paymentKey,
			transactionKey,
			paidAmount,
			method
		);
	}
}
