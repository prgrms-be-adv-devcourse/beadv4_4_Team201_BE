package app.giftify.cart.core.domain;

import app.giftify.shared.domain.type.TargetType;

public record CartItemKey(TargetType targetType, Long targetId) {
    public CartItemKey {
        if (targetType == null) {
            throw new IllegalArgumentException("TargetType은 필수입니다.");
        }
        if (targetId == null) {
            throw new IllegalArgumentException("TargetId는 필수입니다.");
        }
    }
}