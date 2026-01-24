package app.giftify.wishlist.adapter.out.jpa.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.wishlist.adapter.out.jpa.mapper.WishlistProductReplicaMapper;
import app.giftify.wishlist.adapter.out.jpa.repository.WishlistProductReplicaJpaRepository;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WishlistProductReplicaAdapter implements WishlistProductReplicaPort {

	private final WishlistProductReplicaJpaRepository repository;

	// 상품 레플리카 조회
	@Override
	@Transactional(readOnly = true)
	public Optional<WishlistProductReplica> findByProductId(Long productId) {
		return repository.findByProductId(productId)
			.map(WishlistProductReplicaMapper::toDomain);
	}

	// 상품 레플리카 갱신
	@Override
	@Transactional
	public void upsert(WishlistProductReplica domain) {
		repository.findByProductId(domain.getProductId())
			.ifPresentOrElse(
				entity -> entity.update(
					domain.isWishlistAllowed(),
					domain.getUpdatedAt(),
					domain.getName(),
					domain.getPrice(),
					domain.getSellerNickname()
				),
				() -> repository.save(WishlistProductReplicaMapper.toEntity(domain))
			);
	}
}
