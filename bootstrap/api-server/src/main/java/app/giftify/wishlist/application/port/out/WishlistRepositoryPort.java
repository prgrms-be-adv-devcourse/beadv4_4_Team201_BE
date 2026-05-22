package app.giftify.wishlist.application.port.out;

import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;

import java.util.List;
import java.util.Optional;

public interface WishlistRepositoryPort {

    // wishlistId로 위시리스트 찾기
    Optional<Wishlist> findById(Long id);

    // memberId로 위시리스트 찾기
    Optional<Wishlist> findByMemberId(Long memberId);

    // 위시리스트 저장
    Wishlist save(Wishlist wishlist);

    // wishlistIds 로 위시리스트 리스트 조회
    List<Wishlist> findAllById(List<Long> wishlistIds);

    // 특정 사용자의 공개 위시리스트 조회 (개발용)
    Optional<Wishlist> findByMemberIdAndVisibility(Long memberId, Visibility visibility);

    List<Wishlist> findByMemberIdInAndVisibility(List<Long> memberIds, Visibility visibility);

    Optional<Wishlist> findByMemberIdAndVisibilityIn(Long memberId, List<Visibility> visibilities);
}
