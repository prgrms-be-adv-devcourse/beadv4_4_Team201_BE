package app.giftify.cart.application.inbound;

import app.giftify.cart.core.domain.Cart;

public interface AddCartItemUseCase {
    Cart addItem(Long memberId, AddCartItemCommand command);
}
