package app.giftify.wishlist.application.support;

import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WishlistSupport { // 조회 + 예외처리용 헬퍼
    private final WishlistRepositoryPort wishlistRepositoryPort;
    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

    /// WISHLIST ///

    // 위시리스트 id로 위시리스트 조회
    public Wishlist getWishlistById(Long id) {
        return wishlistRepositoryPort.findById(id).orElseThrow(WishlistNotFoundException::new);
    }

    // List<Long> 위시리스트 id로 위시리스트 리스트 조회
    public List<Wishlist> getWishlistAllById(List<Long> wishlistIds) {
        return wishlistRepositoryPort.findAllById(wishlistIds);
    }

    // 멤버 id로 위시리스트 조회
    public Wishlist getWishlistByMemberId(Long memberId) {
        return wishlistRepositoryPort.findByMemberId(memberId).orElseThrow(() -> new WishlistNotFoundException(memberId));
    }


    /// WISHLIST_ITEM ///

    // 위시리스트 id + 상품 id로 위시리스트아이템 조회
    public WishlistItem getWishlistItemByWishlistIdAndProductId(Long wishlistId, Long productId) {
        return wishlistItemRepositoryPort.findByWishlistIdAndProductId(wishlistId, productId)
                .orElseThrow(WishlistItemNotFoundException::new);
    }

    // 위시리스트아이템 id로 위시리스트아이템 조회
    public WishlistItem getWishlistItemById(Long wishlistItemId) {
        return wishlistItemRepositoryPort.findById(wishlistItemId)
                .orElseThrow(WishlistItemNotFoundException::new);
    }

    // List<Long> 위시리스트아이템 id 로 위시리스트아이템 리스트 조회
    public List<WishlistItem> getWishlistItemListById(List<Long> wishlistItemIds) {
        return wishlistItemRepositoryPort.findAllById(wishlistItemIds);
    }

}
