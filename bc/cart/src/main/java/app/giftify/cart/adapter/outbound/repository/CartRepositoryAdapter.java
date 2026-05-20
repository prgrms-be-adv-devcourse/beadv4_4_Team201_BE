package app.giftify.cart.adapter.outbound.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.giftify.cart.adapter.outbound.mapper.CartMapper;
import app.giftify.cart.application.outbound.CartRepositoryPort;
import app.giftify.cart.core.domain.Cart;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CartRepositoryAdapter implements CartRepositoryPort {
	private final JpaCartRepository jpaCartRepository;
	private final CartMapper cartMapper;

	@Override
	public Optional<Cart> findById(Long cartId) {
		return jpaCartRepository.findById(cartId)
			.map(cartMapper::toDomain);
	}

	@Override
	public Optional<Cart> findByMemberId(Long memberId) {
		return jpaCartRepository.findByMemberId(memberId)
			.map(cartMapper::toDomain);
	}

	@Override
	public Cart save(Cart cart) {
		return cartMapper.toDomain(jpaCartRepository.save(cartMapper.toJpaEntity(cart)));
	}

}
