package app.giftify.wishlist.adapter.out.jpa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistProductReplicaJpaEntity;

public interface WishlistProductReplicaJpaRepository extends JpaRepository<WishlistProductReplicaJpaEntity, Long> {
    Optional<WishlistProductReplicaJpaEntity> findByProductId(Long productId);
}
