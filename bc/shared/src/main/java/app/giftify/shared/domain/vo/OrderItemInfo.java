package app.giftify.shared.domain.vo;

import java.time.LocalDateTime;
import java.util.Objects;

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
        Objects.requireNonNull(orderId, "orderId는 필수입니다.");
        Objects.requireNonNull(orderNumber, "orderNumber는 필수입니다.");
        Objects.requireNonNull(orderItemId, "orderItemId는 필수입니다.");
        Objects.requireNonNull(sellerId, "sellerId는 필수입니다.");
        Objects.requireNonNull(quantity, "quantity는 필수입니다.");
        Objects.requireNonNull(totalAmount, "itemTotalAmount는 필수입니다.");
        Objects.requireNonNull(orderedAt, "orderedAt는 필수입니다.");
    }
}