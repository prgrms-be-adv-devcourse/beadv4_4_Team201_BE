package app.giftify.settlement.domain;

import java.time.LocalDateTime;

public record OrderItemInfo(
    Long orderId,
    String orderNumber,
    Long orderItemId,
    Long sellerId,
    Money amount,
    LocalDateTime orderedAt
) {
    public OrderItemInfo {
        if (orderId == null) throw new IllegalArgumentException("orderId는 필수입니다.");
        if (orderNumber == null) throw new IllegalArgumentException("orderNumber는 필수입니다.");
        if (orderItemId == null) throw new IllegalArgumentException("orderItemId는 필수입니다.");
        if (sellerId == null) throw new IllegalArgumentException("sellerId는 필수입니다.");
        if (amount == null) throw new IllegalArgumentException("itemAmount는 필수입니다.");
        if (orderedAt == null) throw new IllegalArgumentException("orderedAt는 필수입니다.");
    }
}