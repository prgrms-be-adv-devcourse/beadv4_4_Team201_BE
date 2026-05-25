package app.giftify.wishlist.application.port.out;

import app.giftify.support.common.api.paging.Page;
import app.giftify.support.common.api.paging.PageRequest;
import app.giftify.wishlist.core.domain.WishlistItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WishlistItemRepositoryPort {
    // 위시리스트 내부의 특정 상품 찾기
    Optional<WishlistItem> findByWishlistIdAndProductId(Long wishlistId, Long productId);

    Optional<WishlistItem> findById(Long id);

    // 특정 위시리스트의 모든 상품 조회
    List<WishlistItem> findByWishlistId(Long wishlistId);

    // 특정 위시리스트의 상품 페이지네이션 조회
    Page<WishlistItem> findByWishlistId(Long wishlistId, PageRequest pageRequest);

    // List<Long> 위시리스트아이템 id 로 위시리스트아이템 리스트 조회
    List<WishlistItem> findAllById(List<Long> wishlistItemIds);

    // 위시리스트에 상품 등록
    WishlistItem save(WishlistItem wishlistItem);

    // 위시리스트에서 특정 상품 삭제
    void deleteByWishlistIdAndProductId(Long wishlistId, Long productId);

    void delete(WishlistItem wishlistItem);

    int deleteCompletedItemsUpdatedBefore(LocalDateTime cutoff);

    Long count();
}
