package app.giftify.shared.domain.event.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentCancelFailedEvent(
        String id, String source, String type, Instant time,
        PaymentCancelFailedData data
) implements PaymentEvent {
    public static PaymentCancelFailedEvent create(PaymentCancelFailedData data) {
        return new PaymentCancelFailedEvent(
                UUID.randomUUID().toString(), "payment", "payment.cancel.failed", Instant.now(), data
        );
    }
}
