package app.giftify.cart.application.outbound;

import java.util.Optional;

import app.giftify.cart.core.domain.Cart;

public interface CartRepositoryPort {

    Cart save(Cart cart);

    Optional<Cart> findById(Long cartId);

    Optional<Cart> findByMemberId(Long memberId);
}
