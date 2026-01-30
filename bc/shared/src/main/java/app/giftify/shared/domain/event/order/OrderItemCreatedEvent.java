package app.giftify.shared.domain.event.order;

import java.math.BigDecimal;

/**
 * targetType == FUNDING 일 때만 발행
 * @param orderItemId
 * @param orderId
 * @param sellerId
 * @param quantity
 * @param unitPrice
 * @param totalAmount
 */
public record OrderItemCreatedEvent(
        Long orderItemId,
        Long fundingId,
        Long orderId,
        Long sellerId,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {
}
