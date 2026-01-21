package app.giftify.wishlist.application.port.out;

import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;

import java.util.Optional;

public interface WishlistProductReplicaPort {

    // replica 조회
    Optional<WishlistProductReplica> findByProductId(Long productId);

    // replica 갱신
    void upsert(WishlistProductReplica replica);
}
