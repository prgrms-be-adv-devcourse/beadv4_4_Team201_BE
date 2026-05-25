package app.giftify.order.domain.event;

import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String orderNumber,
        LocalDateTime orderedAt
) {
}
