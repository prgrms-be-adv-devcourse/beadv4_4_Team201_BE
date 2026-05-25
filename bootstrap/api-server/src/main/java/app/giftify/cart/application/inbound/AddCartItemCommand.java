package app.giftify.cart.application.inbound;

import app.giftify.support.common.money.Money;

public record AddCartItemCommand(
        Long wishlistId,
        Long wishlistItemId,
        Money amount
) {
}
