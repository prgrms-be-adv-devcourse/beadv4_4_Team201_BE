package app.giftify.cart.application.inbound;

import app.giftify.cart.core.domain.CartItemKey;
import app.giftify.shared.domain.vo.Money;

public record AddCartItemCommand(
        CartItemKey cartItemKey,
        Money amount
) {
}