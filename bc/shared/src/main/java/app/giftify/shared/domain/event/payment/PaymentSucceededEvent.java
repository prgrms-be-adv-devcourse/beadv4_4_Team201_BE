package app.giftify.shared.domain.event.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
	String id, String source, String type, Instant time, PaymentEventData data
) implements PaymentEvent {
	public static PaymentSucceededEvent create(PaymentEventData data) {
		return new PaymentSucceededEvent(
			UUID.randomUUID().toString(), "payment", "payment.succeeded", Instant.now(), data);
	}
}
