package app.giftify.orderDemo.domain;

import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import lombok.Builder;

@Builder
public record OrderItemSnapshot(
        Long orderItemId,
        Long targetId,
        TargetType targetType,
        Long sellerId,
        Long receiverId,
        Money price,
        Money amount,
        OrderItemStatus status
) {
}
