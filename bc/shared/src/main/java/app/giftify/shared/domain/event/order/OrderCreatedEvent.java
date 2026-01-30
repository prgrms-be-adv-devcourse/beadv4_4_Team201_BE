package app.giftify.shared.domain.event.order;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        LocalDateTime orderedAt
) {
}
