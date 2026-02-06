package app.giftify.cart.adapter.outbound.repository;

import app.giftify.cart.adapter.outbound.mapper.CartMapper;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CartRepositoryAdapter implements CartRepositoryPort {
    private final JpaCartRepository jpaCartRepository;
    private final CartMapper cartMapper;

    @Override
    public Optional<Cart> findById(Long cartId) {
        return Optional.ofNullable(jpaCartRepository.findById(cartId)
                .map(cartMapper::toDomain)
                .orElse(null));
    }

    @Override
    public Cart save(Cart cart) {
        return cartMapper.toDomain(jpaCartRepository.save(cartMapper.toJpaEntity(cart)));
    }
}
