package app.giftify.cart.application.outbound;

import app.giftify.cart.core.domain.Cart;

import java.util.Optional;

public interface CartRepositoryPort {

    Cart save(Cart cart);

    Optional<Cart> findById(Long cartId);
}
