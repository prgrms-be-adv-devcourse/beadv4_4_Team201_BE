package app.giftify.cart.application.inbound;

import app.giftify.shared.domain.vo.Money;

public record AddCartItemCommand(
        Long wishlistId,
        Long wishlistItemId,
        Money amount
) {
}
