package app.giftify.member.adapter.out.jpa.respository.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface WishlistJpaRepository extends JpaRepository<WishlistJpaEntity, Long> {

    Optional<WishlistJpaEntity> findByAuthSub(String authSub);
}
