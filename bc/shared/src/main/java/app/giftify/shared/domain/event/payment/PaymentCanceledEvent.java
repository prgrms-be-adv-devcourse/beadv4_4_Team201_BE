package app.giftify.shared.domain.event.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentCanceledEvent(
	String id, String source, String type, Instant time, PaymentEventData data
) implements PaymentEvent {
	public static PaymentCanceledEvent create(PaymentEventData data) {
		return new PaymentCanceledEvent(
			UUID.randomUUID().toString(), "payment", "payment.canceled", Instant.now(), data);
	}
}
