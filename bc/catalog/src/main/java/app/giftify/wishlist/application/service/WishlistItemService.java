package app.giftify.wishlist.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import app.giftify.wishlist.application.port.in.AddWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemSnapshotUseCase;
import app.giftify.wishlist.application.port.in.GetWishlistItemUseCase;
import app.giftify.wishlist.application.port.in.RemoveWishlistItemUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.application.support.WishlistSupport;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.DuplicateWishlistItemException;
import app.giftify.wishlist.core.domain.exception.ProductNotOnSaleException;
import app.giftify.wishlist.core.domain.exception.WishlistItemNotRemovableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static app.giftify.wishlist.core.domain.WishlistItemStatus.PENDING;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistItemService implements AddWishlistItemUseCase, GetWishlistItemUseCase, RemoveWishlistItemUseCase, GetWishlistItemSnapshotUseCase {

    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
    private final WishlistRepositoryPort wishlistRepositoryPort;

    private final ProductSupport productSupport;
    private final WishlistSupport wishlistSupport;

    /**
     * 위시리스트아이템 개수 카운팅
     */
    @Override
    @Transactional(readOnly = true)
    public Long getWishlistItemCount() {
        return wishlistItemRepositoryPort.count();
    }

    /**
     * 위시리스트아이템 단건 조회
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isItemExists(Long memberId, Long productId) {
        Wishlist wishlist = getOrCreateWishlistByMemberId(memberId);

        Optional<WishlistItem> wishlistItem = wishlistItemRepositoryPort.findByWishlistIdAndProductId(
                wishlist.getId(),
                productId
        );
        return wishlistItem.isPresent();
    }

    /**
     * 위시리스트아이템 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<WishlistItem> getWishlistItems(Long memberId) {
        Wishlist wishlist = getOrCreateWishlistByMemberId(memberId);
        return wishlistItemRepositoryPort.findByWishlistId(wishlist.getId());
    }

    /**
     * 위시리스트아이템 추가
     * # 이미 위시리스트에 등록된 상품은 등록할 수 없습니다.
     * # 상품을 조회하여 상태가 ACTIVE인 상품만 위시리스트아이템 추가 허용
     */
    @Override
    @Transactional
    public WishlistItem addWishlistItem(Long memberId, WishlistItemAddCommand command) {
        // 위시리스트 확인, 없으면 생성
        Wishlist wishlist = getOrCreateWishlistByMemberId(memberId);

        // 상품 확인 (Active인 상품만 위시리스트아이템 추가 가능)
        Product product = productSupport.findById(command.productId());
        if (!product.getStatus().equals(ProductStatus.ACTIVE))
            throw new ProductNotOnSaleException(command.productId());

        // 위시리스트 DB에 상품 id가 있는지 조회 (중복 확인)
        if (wishlistItemRepositoryPort.findByWishlistIdAndProductId(wishlist.getId(), product.getId())
                .isPresent()) {
            throw new DuplicateWishlistItemException(wishlist.getId(), command.productId());
        }

        // 위시리스트 item에 등록
        WishlistItem wishlistItem = WishlistItem.builder()
                .wishlistId(wishlist.getId())
                .productId(product.getId())
                .wishlistItemStatus(PENDING)
                .build();

        // 새롭게 생성된 WishlistItem 반환
        return wishlistItemRepositoryPort.save(wishlistItem);

        // todo 알림 이벤트
    }

    /**
     * # 위시리스트아이템 수동 삭제
     * - PENDING 상태일 때만 수동 삭제 가능
     */
    @Override
    @Transactional
    public void removeWishlistItem(WishlistItemRemoveCommand command) {
        Wishlist wishlist = wishlistSupport.getWishlistByMemberId(command.memberId());
        WishlistItem wishlistItem = wishlistSupport.getWishlistItemByWishlistIdAndProductId(wishlist.getId(), command.productId());

        validateManualRemovable(wishlistItem); // 상태 확인
        wishlistItemRepositoryPort.delete(wishlistItem);

        // todo 장바구니아이템 상태 변경
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistItemSnapshot getSnapshot(Long wishlistItemId) {
        WishlistItem wishlistItem = wishlistSupport.getWishlistItemById(wishlistItemId);
        Product product = productSupport.findById(wishlistItem.getProductId());
        Wishlist wishlist = wishlistSupport.getWishlistById(wishlistItem.getWishlistId());

        return new WishlistItemSnapshot(
                wishlistItem.getId(), product.getId(), product.getName(), product.getPrice(), product.getSellerId(), wishlist.getMemberId()
        );
    }

    /**
     * 위시리스트아이템 스냅샷 조회
     * - 단건 펀딩, 복수 펀딩 모두 사용
     * - Map : O(1)로 매핑 가능 (조회 3번)
     */
    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemSnapshot> getSnapshotList(List<Long> wishlistItemIds) {

        // 1. get wishlistItem (요청 순서 보장용 Map 변환)
        Map<Long, WishlistItem> wishlistItemMap = wishlistSupport.getWishlistItemListById(wishlistItemIds).stream()
                .collect(Collectors.toMap(WishlistItem::getId, Function.identity()));

        // 2. get product
        List<Long> productIds = wishlistItemMap.values().stream().map(WishlistItem::getProductId).distinct().toList();
        List<Product> productList = productSupport.findAllById(productIds);
        productSupport.validatePurchasable(productList); // 상품 검증 (ACTIVE && 재고!=0)

        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 3. get wishlist
        List<Long> wishlistIds = wishlistItemMap.values().stream().map(WishlistItem::getWishlistId).distinct().toList();
        Map<Long, Wishlist> wishlistMap = wishlistSupport.getWishlistAllById(wishlistIds).stream()
                .collect(Collectors.toMap(Wishlist::getId, Function.identity()));

        // 4. 요청 순서대로 스냅샷 생성 및 return
        return wishlistItemIds.stream() //리스트 순회
                .map(id -> {
                    WishlistItem item = wishlistItemMap.get(id);
                    Product product = productMap.get(item.getProductId());
                    Wishlist wishlist = wishlistMap.get(item.getWishlistId());
                    return new WishlistItemSnapshot(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getSellerId(),
                            wishlist.getMemberId()
                    );
                })
                .toList();
    }

    // memberId로 Wishlist 조회 없으면 생성
    private Wishlist getOrCreateWishlistByMemberId(Long memberId) {
        return wishlistRepositoryPort.findByMemberId(memberId)
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .memberId(memberId)
                            .build();
                    return wishlistRepositoryPort.save(wishlist);
                });
    }

    // 위시리스트아이템의 상태를 체크하여 삭제 가능 여부 검증
    private void validateManualRemovable(WishlistItem item) {
        WishlistItemStatus status = item.getWishlistItemStatus();

        if (status != PENDING) {
            throw new WishlistItemNotRemovableException(status);
        }
    }
}
