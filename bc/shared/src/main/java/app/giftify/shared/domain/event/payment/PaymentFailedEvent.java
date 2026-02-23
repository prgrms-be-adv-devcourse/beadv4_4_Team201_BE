package app.giftify.shared.domain.event.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
	String id, String source, String type, Instant time, PaymentEventData data
) implements PaymentEvent {
	public static PaymentFailedEvent create(PaymentEventData data) {
		return new PaymentFailedEvent(
			UUID.randomUUID().toString(), "payment", "payment.failed", Instant.now(), data);
	}
}
