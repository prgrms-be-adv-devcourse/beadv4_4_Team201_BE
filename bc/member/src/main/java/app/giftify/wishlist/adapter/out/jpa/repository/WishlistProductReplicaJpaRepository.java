package app.giftify.wishlist.adapter.out.jpa.repository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistProductReplicaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistProductReplicaJpaRepository extends JpaRepository<WishlistProductReplicaJpaEntity, Long> {
    Optional<WishlistProductReplicaJpaEntity> findByProductId(Long productId);
}
