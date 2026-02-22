package app.giftify.wishlist.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.application.support.WishlistSupport;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemDetail;
import app.giftify.wishlist.core.domain.exception.WishlistNotAccessibleException;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService implements GetWishlistUseCase, UpdateWishlistSettingsUseCase {

    private final WishlistRepositoryPort wishlistRepositoryPort;
    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
    private final FriendshipVerificationPort friendshipVerificationPort;
    private final ProductSupport productSupport;
    private final WishlistSupport wishlistSupport;

    @Override
    @Transactional
    public Wishlist getOrCreateWishlistByMemberId(Long memberId) {
        return wishlistRepositoryPort.findByMemberId(memberId)
                .orElseGet(() -> {
                    Wishlist wishlist = Wishlist.builder()
                            .memberId(memberId)
                            .build();
                    return wishlistRepositoryPort.save(wishlist);
                });
    }

    /**
     * 타인의 위시리스트 아이템 목록 조회
     * 타겟 멤버와 친구이면 PUBLIC 또는 FRIENDS_ONLY 위시리스트 조회 가능
     * 타겟 멤버와 친구가 아니거나, 비로그인 상태이면 PUBLIC 위시리스트일 때만 조회 가능
     */
    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemDetail> getWishlistItemDetails(Long targetMemberId, Long currentMemberId) {
        Wishlist wishlist = wishlistSupport.getWishlistByMemberId(targetMemberId);

        // Visibility 체크
        List<Visibility> visibilities;
        if (currentMemberId != null && friendshipVerificationPort.areFriends(currentMemberId, targetMemberId)) {
            visibilities = List.of(Visibility.PUBLIC, Visibility.FRIENDS_ONLY);
        } else {
            visibilities = List.of(Visibility.PUBLIC);
        }

        if (!visibilities.contains(wishlist.getVisibility())) {
            throw new WishlistNotAccessibleException(); // 조회 권한이 없는 위시리스트일 때
        }

        List<WishlistItem> items = wishlistItemRepositoryPort.findByWishlistId(wishlist.getId());
        return toItemDetails(items);
    }

    @Override
    @Transactional
    public Wishlist updateSettings(UpdateSettingsCommand command) {
        Wishlist wishlist = wishlistRepositoryPort.findByMemberId(command.memberId())
                .orElseThrow(() -> new WishlistNotFoundException(command.memberId()));

        wishlist.changeVisibility(command.visibility());

        Wishlist updatedWishlist = wishlistRepositoryPort.save(wishlist);

        // 위시리스트 상태 변경 이벤트 발행 todo

        return updatedWishlist;
    }

    private List<WishlistItemDetail> toItemDetails(List<WishlistItem> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = items.stream().map(WishlistItem::getProductId).toList();
        Map<Long, Product> productMap = productSupport.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return items.stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            return new WishlistItemDetail(
                    item,
                    product != null ? product.getName() : null,
                    product != null ? product.getPrice() : 0,
                    product != null ? product.getImageKey() : null,
                    product != null && product.getStock() == 0, // 품절 여부
                    product != null && product.getStatus() == ProductStatus.ACTIVE // 활성화 여부
            );
        }).toList();
    }
}
