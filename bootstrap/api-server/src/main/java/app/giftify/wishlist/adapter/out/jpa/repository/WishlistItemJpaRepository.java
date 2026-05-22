package app.giftify.wishlist.adapter.out.jpa.repository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WishlistItemJpaRepository extends JpaRepository<WishlistItemJpaEntity, Long> {

    // 위시리스트에서 특정 상품 삭제
    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);

    List<WishlistItemJpaEntity> findByWishlistId(Long wishlistId);

    Page<WishlistItemJpaEntity> findByWishlistId(Long wishlistId, Pageable pageable);

    Optional<WishlistItemJpaEntity> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM WishlistItemJpaEntity w WHERE w.wishlistItemStatus = 'COMPLETED' AND w.updatedAt < :cutoff")
    int deleteCompletedItemsUpdatedBefore(@Param("cutoff") LocalDateTime cutoff);
}
