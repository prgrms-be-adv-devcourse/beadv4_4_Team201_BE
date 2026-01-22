package app.giftify.wishlist.adapter.out.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistItemJpaEntity;

public interface WishlistItemJpaRepository extends JpaRepository<WishlistItemJpaEntity, Long> {

	// 위시리스트에서 특정 상품 삭제
	// void deleteByAuthSubAndProductId(String authSub, Long productId);
	//
	// Optional<WishlistItemJpaEntity> findByAuthSubAndProductId(String authSub, Long productId);
	//
	// List<WishlistItemJpaEntity> findByAuthSub(String authSub);

	void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);

	// Optional<WishlistItemJpaEntity> findByMemberIdAndProductId(Long memberId, Long productId);

	List<WishlistItemJpaEntity> findByWishlistId(Long wishlistId);

	Optional<WishlistItemJpaEntity> findByWishlistIdAndProductId(Long wishlistId, Long productId);
}
