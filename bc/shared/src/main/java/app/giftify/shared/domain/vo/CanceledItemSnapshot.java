package app.giftify.shared.domain.vo;

import app.giftify.shared.domain.type.TargetType;

public record CanceledItemSnapshot(
        Long orderItemId,
        Long buyerId,
        Long targetId,
        TargetType targetType,
        Money cancelAmount
) {
}
