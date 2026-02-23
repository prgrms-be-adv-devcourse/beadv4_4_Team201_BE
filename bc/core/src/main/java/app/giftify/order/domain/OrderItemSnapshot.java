package app.giftify.order.domain;

import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import lombok.Builder;

@Builder
public record OrderItemSnapshot(
        Long orderItemId,
        Long orderId,
        Long targetId,
        TargetType targetType,
        OrderItemType orderItemType,
        Long sellerId,
        Long receiverId,
        Money price,
        Money amount,
        OrderItemStatus status
) {
}
