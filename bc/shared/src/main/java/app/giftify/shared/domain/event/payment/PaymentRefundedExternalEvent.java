package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.giftify.shared.domain.vo.Money;

public record PaymentRefundedExternalEvent(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	Money refundAmount,
	List<Long> sellerIds
) implements PaymentExternalEvent {

	public PaymentRefundedExternalEvent {
		sellerIds = sellerIds != null ? List.copyOf(sellerIds) : List.of();
	}

	public static PaymentRefundedExternalEvent create(
		Long paymentId,
		Money refundAmount,
		List<Long> sellerIds,
		LocalDateTime occurredAt
	) {
		return new PaymentRefundedExternalEvent(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			refundAmount,
			sellerIds
		);
	}
}
