package app.giftify.wishlist.application.service;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductStatus;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.paging.Page;
import app.giftify.shared.api.paging.PageRequest;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import app.giftify.wishlist.application.port.in.GetWishlistUseCase;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.in.WishlistItemDetail;
import app.giftify.wishlist.application.port.in.WishlistOverview;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.application.support.WishlistSupport;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
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
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Wishlist getOrCreateWishlistByMemberId(Long memberId) {
        return wishlistSupport.getOrCreateWishlistByMemberId(memberId);
    }

    // 내 위시리스트 조회 Overview
    @Override
    @Transactional(readOnly = true)
    public WishlistOverview getMyWishlistOverview(Long memberId, PageRequest pageRequest) {
        Wishlist wishlist = wishlistSupport.getOrCreateWishlistByMemberId(memberId);
        String ownerNickname = findNickname(memberId);
        Page<WishlistItemDetail> itemPage = getWishlistItemDetails(wishlist.getId(), pageRequest);

        return new WishlistOverview(wishlist, ownerNickname, itemPage);
    }

    // 특정 회원의 위시리스트 조회 Overview
    @Override
    @Transactional(readOnly = true)
    public WishlistOverview getWishlistOverview(Long targetMemberId, Long currentMemberId, PageRequest pageRequest) {
        Wishlist wishlist = wishlistSupport.getWishlistByMemberId(targetMemberId);

        // 본인 위시리스트는 항상 접근 가능
        boolean isOwner = currentMemberId != null && currentMemberId.equals(targetMemberId);
        if (isOwner)
            return getMyWishlistOverview(currentMemberId, pageRequest);

        /**
         * 타겟 멤버와 친구이면 PUBLIC 또는 FRIENDS_ONLY 위시리스트 조회 가능
         * 타겟 멤버와 친구가 아니거나, 비로그인 상태이면 PUBLIC 위시리스트일 때만 조회 가능
         */
        // Visibility 체크
        List<Visibility> visibilities;
        if (currentMemberId != null && friendshipVerificationPort.areFriends(currentMemberId, targetMemberId)) {
            visibilities = List.of(Visibility.PUBLIC, Visibility.FRIENDS_ONLY);
        } else {
            visibilities = List.of(Visibility.PUBLIC);
        }

        if (!visibilities.contains(wishlist.getVisibility())) {
            throw new WishlistNotAccessibleException();
        }

        Page<WishlistItemDetail> itemPage = getWishlistItemDetails(wishlist.getId(), pageRequest);
        String ownerNickname = findNickname(targetMemberId);

        return new WishlistOverview(wishlist, ownerNickname, itemPage);
    }

    // 위시리스트 아이템 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<WishlistItemDetail> getWishlistItemDetails(Long wishlistId, PageRequest pageRequest) {

        Page<WishlistItem> itemPage = wishlistItemRepositoryPort.findByWishlistId(wishlistId, pageRequest);
        List<WishlistItemDetail> details = toItemDetails(itemPage.content());
        return Page.of(details, itemPage.totalElements());
    }

    // 위시리스트 공개 범위 설정
    @Override
    @Transactional
    public Wishlist updateSettings(UpdateSettingsCommand command) {
        Wishlist wishlist = wishlistRepositoryPort.findByMemberId(command.memberId())
                .orElseThrow(() -> new WishlistNotFoundException(command.memberId()));

        wishlist.changeVisibility(command.visibility());
        Wishlist updatedWishlist = wishlistRepositoryPort.save(wishlist);

        return updatedWishlist;
    }

    private List<WishlistItemDetail> toItemDetails(List<WishlistItem> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = items.stream().map(WishlistItem::getProductId).toList();
        Map<Long, Product> productMap = productSupport.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Long> sellerIds = productMap.values().stream()
                .map(Product::getSellerId)
                .distinct()
                .toList();
        Map<Long, String> sellerNicknameMap = memberRepository.findAllById(sellerIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        // 삭제된 상품은 목록에서 제외
        return items.stream()
                .filter(item -> productMap.containsKey(item.getProductId()))
                .map(item -> {
                    Product product = productMap.get(item.getProductId());
                    String sellerNickname = sellerNicknameMap.get(product.getSellerId());

                    return new WishlistItemDetail(
                            item,
                            product.getName(),
                            product.getPrice(),
                            product.getImageKey(),
                            product.getStock() == 0,
                            product.getStatus() == ProductStatus.ACTIVE,
                            sellerNickname,
                            product.getCategory()
                    );
                }).toList();
    }

    private String findNickname(Long memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getNickname)
                .orElse(null);
    }
}
