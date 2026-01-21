package app.giftify.wishlist.application.service;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import app.giftify.wishlist.application.port.out.WishlistProductReplicaPort;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.exception.DuplicateWishlistItemException;
import app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import app.giftify.wishlist.core.domain.replica.WishlistProductReplica;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistItemService implements AddWishlistItemUseCase, GetWishlistItemUseCase, RemoveWishlistItemUseCase {

    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
    private final WishlistProductReplicaPort wishlistProductReplicaPort;
    private final WishlistProductQueryPort wishlistProductQueryPort;
    private final EventPublisher eventPublisher;

    private static final java.time.Duration REPLICA_TTL = Duration.ofHours(1);

    @Override
    @Transactional(readOnly = true)
    public Long getWishlistItemCount() {
        return wishlistItemRepositoryPort.count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isItemExists(String authSub, Long productId) {
        Optional<WishlistItem> wishlistItem = wishlistItemRepositoryPort.findByAuthSubAndProductId(authSub, productId);
        return wishlistItem.isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItem> getWishlistItems(String authSub) {
        return wishlistItemRepositoryPort.findByAuthSub(authSub);
    }

    @Override
    @Transactional
    public WishlistItem addWishlistItem(WishlistItemAddCommand command) {
        // 위시리스트 DB에 상품 id가 있는지 조회 (중복 확인)
        if (wishlistItemRepositoryPort.findByAuthSubAndProductId(command.authSub(), command.productId()).isPresent()) {
            throw new DuplicateWishlistItemException(command.authSub(), command.productId());
        }

        // 상품 레플리카 조회 및 상태 검증
        // 없으면 레플리카 생성
        validateProductActive(command.productId());

        // 3. 위시리스트 item에 등록
        WishlistItem wishlistItem = WishlistItem.builder()
                .authSub(command.authSub())
                .productId(command.productId())
                .itemStatus(command.itemStatus())
                .build();
        WishlistItem addedWishlistItem = wishlistItemRepositoryPort.save(wishlistItem);

        // 4. 이벤트 발행 (현재 구현 생략)

        // 5. 새롭게 생성된 WishlistItem 반환
        return addedWishlistItem;
    }

    // 상품이 판매 중인지 검증
    private void validateProductActive(Long productId) {
        Optional<WishlistProductReplica> replica = wishlistProductReplicaPort.findByProductId(productId);

        // 레플리카가 있고 신선한(Fresh) 상태라면 바로 사용
        if (replica.isPresent() && replica.get().isFresh(REPLICA_TTL)) {
            if (!replica.get().isWishlistAllowed()) {
                throw new ProductNotOnSaleException(productId);
            }
            return;
        }

        // 레플리카가 없거나 만료된 경우 Fallback (직접 조회)
        // 요구사항: /api/product/{id}로 API 호출해서 가져온 후 db에 저장/갱신
        WishlistProductQueryPort.ProductStatus status = wishlistProductQueryPort.getProductStatus(productId);

        // 조회된 정보로 레플리카 갱신 (Upsert)
        WishlistProductReplica newReplica = WishlistProductReplica.builder()
                .productId(productId)
                .wishlistAllowed(status.onSale())
                .name(status.name())
                .price(status.price())
                .sellerNickName(status.sellerNickName())
                .build();
        wishlistProductReplicaPort.upsert(newReplica);

        if (!status.onSale()) {
            throw new ProductNotOnSaleException(productId);
        }
    }

    @Override
    @Transactional
    public void removeWishlistItem(WishlistItemRemoveCommand command) {
        WishlistItem wishlistItem = wishlistItemRepositoryPort
                .findByAuthSubAndProductId(command.authSub(), command.productId())
                .orElseThrow(() -> new WishlistNotFoundException(command.authSub()));

        wishlistItemRepositoryPort.delete(wishlistItem);

        // 위시리스트 아이템 삭제 이벤트 발행
    }
}
