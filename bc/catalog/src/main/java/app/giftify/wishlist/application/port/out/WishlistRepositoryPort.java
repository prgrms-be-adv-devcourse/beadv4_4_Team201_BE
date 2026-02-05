package app.giftify.wishlist.application.port.out;

import app.giftify.wishlist.core.domain.Wishlist;

import java.util.Optional;

public interface WishlistRepositoryPort {

    // wishlistId로 위시리스트 찾기
    Optional<Wishlist> findById(Long id);

    // memberId로 위시리스트 찾기
    Optional<Wishlist> findByMemberId(Long memberId);

    // 위시리스트 저장
    Wishlist save(Wishlist wishlist);
}
