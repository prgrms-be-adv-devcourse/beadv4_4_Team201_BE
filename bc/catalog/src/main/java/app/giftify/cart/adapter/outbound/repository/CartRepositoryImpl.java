package app.giftify.cart.adapter.outbound.repository;

import app.giftify.cart.adapter.outbound.mapper.CartMapper;
import app.giftify.cart.application.outbound.CartRepository;
import app.giftify.cart.core.domain.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CartRepositoryImpl implements CartRepository {
    private final JpaCartRepository jpaCartRepository;
    private final CartMapper cartMapper;

    @Override
    public Optional<Cart> findByMemberId(Long memberId) {
        return Optional.ofNullable(jpaCartRepository.findByMemberId(memberId)
                .map(cartMapper::toDomain)
                .orElse(null));
    }

    @Override
    public Cart save(Cart cart) {
        return cartMapper.toDomain(jpaCartRepository.save(cartMapper.toJpaEntity(cart)));
    }
}
