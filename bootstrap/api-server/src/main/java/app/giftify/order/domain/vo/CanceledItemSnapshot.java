package app.giftify.order.domain.vo;

import app.giftify.order.domain.type.TargetType;
import app.giftify.support.common.money.Money;

public record CanceledItemSnapshot(
        Long orderItemId,
        Long buyerId,
        Long targetId,
        TargetType targetType,
        Money cancelAmount
) {
}
