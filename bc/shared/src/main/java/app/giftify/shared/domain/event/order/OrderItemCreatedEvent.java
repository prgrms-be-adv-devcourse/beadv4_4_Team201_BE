package app.giftify.shared.domain.event.order;

import java.math.BigDecimal;

public record OrderItemCreatedEvent(
        Long orderItemId,
        Long sellerId,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
