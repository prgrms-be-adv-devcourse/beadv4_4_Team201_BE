package app.giftify.cart.application.inbound.usecase;

import app.giftify.cart.core.domain.Cart;

public interface CartCreateUseCase {
    Cart createCart(Long memberId);
}

