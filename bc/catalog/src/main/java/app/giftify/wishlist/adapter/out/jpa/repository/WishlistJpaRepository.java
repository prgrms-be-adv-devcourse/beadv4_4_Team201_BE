package app.giftify.wishlist.adapter.out.jpa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.core.domain.Visibility;

public interface WishlistJpaRepository extends JpaRepository<WishlistJpaEntity, Long> {
	Optional<WishlistJpaEntity> findByMemberId(Long memberId);

	Optional<WishlistJpaEntity> findByMemberIdAndVisibility(Long memberId, Visibility visibility);

	List<WishlistJpaEntity> findByMemberIdInAndVisibility(List<Long> memberIds, Visibility visibility);
}
