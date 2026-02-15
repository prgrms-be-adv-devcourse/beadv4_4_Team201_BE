package app.giftify.cart.core.domain;

import app.giftify.cart.core.domain.exception.CartException;
import app.giftify.shared.domain.type.TargetType;

import static app.giftify.cart.core.domain.exception.CartErrorCode.CARTITEM_ID_REQUIRED;
import static app.giftify.cart.core.domain.exception.CartErrorCode.CARTITEM_TYPE_REQUIRED;

public record CartItemKey(TargetType targetType, Long targetId) {
    public CartItemKey {
        if (targetType == null) {
            throw new CartException(CARTITEM_TYPE_REQUIRED);
        }
        if (targetId == null) {
            throw new CartException(CARTITEM_ID_REQUIRED);
        }
    }
}