package app.giftify.shared.domain.event.payment;

import java.time.Instant;

public sealed interface PaymentEvent
	permits PaymentSucceededEvent,
			PaymentFailedEvent,
			PaymentCanceledEvent,
			PaymentCancelFailedEvent {

	String id();
	String source();
	String type();
	Instant time();
	PaymentEventData data();
}
