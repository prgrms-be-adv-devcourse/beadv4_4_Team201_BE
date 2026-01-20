package app.giftify.member.application.port.out.wishlist;

import app.giftify.member.core.domain.wishlist.WishlistItem;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepositoryPort {
    // 위시리스트 내부의 특정 상품 찾기
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    // 위시리스트 아이템 ID로 찾기
    Optional<WishlistItem> findById(Long id);

    // 특정 위시리스트의 모든 상품 조회
    List<WishlistItem> findByWishlistId(Long wishlistId);

    // 위시리스트에 상품 등록
    WishlistItem save(WishlistItem wishlistItem);

    // 위시리스트에서 특정 상품 삭제
    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);

    // 위시리스트에서 특정 아이템 삭제
    void delete(WishlistItem wishlistItem);

    Long count();
}
