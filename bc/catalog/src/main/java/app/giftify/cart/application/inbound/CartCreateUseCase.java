package app.giftify.cart.application.inbound;

import app.giftify.cart.core.domain.Cart;

public interface CartCreateUseCase {
    Cart createCart(Long memberId);
}

