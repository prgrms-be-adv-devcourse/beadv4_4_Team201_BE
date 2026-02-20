package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentCanceledExternalEvent(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	String orderNumber,
	String reason
) implements PaymentExternalEvent {

	public static PaymentCanceledExternalEvent create(
		Long paymentId,
		String orderNumber,
		String reason,
		LocalDateTime occurredAt
	) {
		return new PaymentCanceledExternalEvent(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			orderNumber,
			reason
		);
	}
}
