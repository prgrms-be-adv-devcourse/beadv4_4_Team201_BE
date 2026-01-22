package app.giftify.wishlist.adapter.out.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;

public interface WishlistJpaRepository extends JpaRepository<WishlistJpaEntity, Long> {
}
