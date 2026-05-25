package app.giftify.payment.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
        String id, String source, String type, Instant time,
        PaymentFailureData data
) implements PaymentEvent {
    public static PaymentFailedEvent create(PaymentFailureData data) {
        return new PaymentFailedEvent(
                UUID.randomUUID().toString(), "payment", "payment.failed", Instant.now(), data
        );
    }
}
