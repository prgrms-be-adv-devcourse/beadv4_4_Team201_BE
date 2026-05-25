package app.giftify.payment.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
        String id, String source, String type, Instant time,
        PaymentSuccessData data
) implements PaymentEvent {
    public static PaymentSucceededEvent create(PaymentSuccessData data) {
        return new PaymentSucceededEvent(
                UUID.randomUUID().toString(), "payment", "payment.succeeded", Instant.now(), data
        );
    }
}
