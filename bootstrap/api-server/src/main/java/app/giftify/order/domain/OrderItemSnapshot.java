package app.giftify.order.domain;

import app.giftify.order.domain.type.OrderItemType;
import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;
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
