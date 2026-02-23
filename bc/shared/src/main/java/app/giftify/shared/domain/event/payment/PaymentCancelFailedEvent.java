package app.giftify.shared.domain.event.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentCancelFailedEvent(
	String id, String source, String type, Instant time, PaymentEventData data
) implements PaymentEvent {
	public static PaymentCancelFailedEvent create(PaymentEventData data) {
		return new PaymentCancelFailedEvent(
			UUID.randomUUID().toString(), "payment", "payment.cancel.failed", Instant.now(), data);
	}
}
