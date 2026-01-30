package app.giftify.cart.application.outbound;

import app.giftify.cart.core.domain.Cart;

import java.util.Optional;

public interface CartRepository {
    Optional<Cart> findByMemberId(Long memberId);

    Cart save(Cart cart);
}
