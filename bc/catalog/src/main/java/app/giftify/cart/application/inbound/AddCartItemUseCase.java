package app.giftify.cart.application.inbound;

import app.giftify.cart.core.domain.CartItemAddResult;

public interface AddCartItemUseCase {
    CartItemAddResult addItem(Long cartId, AddCartItemCommand command);
}
