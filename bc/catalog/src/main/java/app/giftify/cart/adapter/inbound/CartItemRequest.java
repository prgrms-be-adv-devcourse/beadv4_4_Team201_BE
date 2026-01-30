package app.giftify.cart.adapter.inbound;

import app.giftify.shared.domain.type.TargetType;

public record CartItemRequest(
        TargetType targetType,
        Long targetId,
        Long amount
) {
}
