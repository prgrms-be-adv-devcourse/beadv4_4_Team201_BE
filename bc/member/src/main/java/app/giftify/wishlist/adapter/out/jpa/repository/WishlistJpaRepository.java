package app.giftify.wishlist.adapter.out.jpa.repository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface WishlistJpaRepository extends JpaRepository<WishlistJpaEntity, Long> {

    Optional<WishlistJpaEntity> findByAuthSub(String authSub);
}
