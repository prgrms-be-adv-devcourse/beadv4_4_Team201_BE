package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public record OrderItemInfo(
    Long orderId,
    String orderNumber,
    Long orderItemId,
    Long sellerId,
    Long quantity,
    Money totalAmount,
    LocalDateTime orderedAt
) {
    public OrderItemInfo {
        if (orderId == null) throw new IllegalArgumentException("orderId는 필수입니다.");
        if (orderNumber == null) throw new IllegalArgumentException("orderNumber는 필수입니다.");
        if (orderItemId == null) throw new IllegalArgumentException("orderItemId는 필수입니다.");
        if (sellerId == null) throw new IllegalArgumentException("sellerId는 필수입니다.");
        if (quantity == null) throw new IllegalArgumentException("quantity는 필수입니다.");
        if (totalAmount == null) throw new IllegalArgumentException("itemTotalAmount는 필수입니다.");
        if (orderedAt == null) throw new IllegalArgumentException("orderedAt는 필수입니다.");
    }
}