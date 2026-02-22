package app.giftify.wishlist.adapter.in.event;

import app.giftify.shared.domain.event.funding.*;
import app.giftify.shared.domain.event.member.MemberSignedEvent;
import app.giftify.shared.domain.vo.FundingDetail;
import app.giftify.wishlist.application.port.out.WishlistItemRepositoryPort;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.WishlistItem;
import app.giftify.wishlist.core.domain.WishlistItemStatus;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishlistEventListener {

    private final WishlistItemRepositoryPort wishlistItemRepositoryPort;
    private final WishlistRepositoryPort wishlistRepositoryPort;

    // 회원생성 시 위시리스트 생성
    @ApplicationModuleListener
    public void handleMemberSigned(MemberSignedEvent event) {
        log.info("[Wishlist] 회원 가입으로 위시리스트를 생성합니다 | memberId: {}", event.getMemberId());

        Wishlist wishlist = Wishlist.builder()
                .memberId(event.getMemberId())
                .build();

        wishlistRepositoryPort.save(wishlist);
    }

    /**
     * 펀딩 - 위시리스트아이템 상태 전이
     */
    @ApplicationModuleListener
    public void handleFundingListCreated(FundingCreatedEventV2 event) {
        List<FundingDetail> fundingDetails = event.getFundings();

        for (FundingDetail detail : fundingDetails) {
            updateStatus(detail.wishlistItemId(),
                    WishlistItemStatus.IN_PROGRESS,
                    "[Wishlist] 위시리스트상품의 펀딩이 시작되었습니다.");
        }
    }

    @ApplicationModuleListener
    public void handleFundingAchieved(FundingAchievedEvent event) {
        updateStatus(event.getWishlistItemId(),
                WishlistItemStatus.REQUESTED_CONFIRM,
                "[Wishlist] 위시리스트상품의 펀딩이 달성되었습니다.");
    }

    @ApplicationModuleListener
    public void handleFundingAccepted(FundingAcceptedEvent event) {
        updateStatus(event.getWishlistItemId(),
                WishlistItemStatus.COMPLETED,
                "[Wishlist] 위시리스트상품의 펀딩이 수락되었습니다.");
    }

    @ApplicationModuleListener
    public void handleFundingCanceled(FundingCanceledEvent event) {
        updateStatus(event.getWishlistItemId(),
                WishlistItemStatus.PENDING,
                "[Wishlist] 위시리스트상품의 펀딩이 거절되었습니다.");
    }

    @ApplicationModuleListener
    public void handleFundingExpired(FundingExpiredEvent event) {
        updateStatus(event.getWishlistItemId(),
                WishlistItemStatus.PENDING,
                "[Wishlist] 위시리스트상품의 펀딩이 만료되었습니다.");
    }

    // 위시리스트아이템 상태 변경 메서드
    private void updateStatus(Long wishlistItemId, WishlistItemStatus next, String message) {
        WishlistItem wishlistItem = wishlistItemRepositoryPort.findById(wishlistItemId)
                .orElseThrow(WishlistNotFoundException::new);

        log.info("{} | wishlistItemId: {}", message, wishlistItem.getId());
        wishlistItem.changeStatus(next);

        wishlistItemRepositoryPort.save(wishlistItem);
    }
}
