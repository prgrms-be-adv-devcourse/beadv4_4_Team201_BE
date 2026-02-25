package app.giftify.wishlist.adapter.out.jpa.repository;

import app.giftify.wishlist.adapter.out.jpa.entity.WishlistJpaEntity;
import app.giftify.wishlist.core.domain.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistJpaRepository extends JpaRepository<WishlistJpaEntity, Long> {
    Optional<WishlistJpaEntity> findFirstByMemberId(Long memberId);

    Optional<WishlistJpaEntity> findByMemberIdAndVisibility(Long memberId, Visibility visibility);

    List<WishlistJpaEntity> findByMemberIdInAndVisibility(List<Long> memberIds, Visibility visibility);

    Optional<WishlistJpaEntity> findByMemberIdAndVisibilityIn(Long memberId, List<Visibility> visibilities);
}
