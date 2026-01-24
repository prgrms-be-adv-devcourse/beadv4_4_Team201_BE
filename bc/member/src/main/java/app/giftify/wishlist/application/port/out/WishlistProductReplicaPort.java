package app.giftify.wishlist.application.port.out;

import java.util.Optional;

import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;

public interface WishlistProductReplicaPort {

	// 상품 레플리카 조회
	Optional<WishlistProductReplica> findByProductId(Long productId);

	// 상품 레플리카 갱신
	void upsert(WishlistProductReplica replica);
}
