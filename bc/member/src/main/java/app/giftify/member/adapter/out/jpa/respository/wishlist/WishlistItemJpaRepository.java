package app.giftify.member.adapter.out.jpa.respository.wishlist;

import app.giftify.member.adapter.out.jpa.entity.wishlist.WishlistItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistItemJpaRepository extends JpaRepository<WishlistItemJpaEntity, Long> {

    // 위시리스트에서 특정 상품 삭제
    void deleteByAuthSubAndProductId(String authSub, Long productId);

    Optional<WishlistItemJpaEntity> findByAuthSubAndProductId(String authSub, Long productId);

    List<WishlistItemJpaEntity> findByAuthSub(String authSub);
}
