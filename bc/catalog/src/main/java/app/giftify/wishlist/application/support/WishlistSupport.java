package app.giftify.wishlist.application.support;

import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.exception.WishlistItemNotFoundException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistSupport { // 조회 + 예외처리용 헬퍼
    private final WishlistRepositoryPort wishlistRepositoryPort;
    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;

    // 위시리스트 id로 위시리스트 조회
    public Wishlist getById(Long id) {
        return wishlistRepositoryPort.findById(id).orElseThrow(WishlistNotFoundException::new);
    }

    // 멤버 id로 위시리스트 조회
    public Wishlist getByMemberId(Long memberId) {
        return wishlistRepositoryPort.findByMemberId(memberId).orElseThrow(() -> new WishlistNotFoundException(memberId));
    }

    // 위시리스트 id + 상품 id로 위시리스트아이템 조회
    public WishlistItem getByWishlistIdAndProductId(Long wishlistId, Long productId) {
        return wishlistItemRepositoryPort.findByWishlistIdAndProductId(wishlistId, productId)
                .orElseThrow(WishlistItemNotFoundException::new);
    }
}
