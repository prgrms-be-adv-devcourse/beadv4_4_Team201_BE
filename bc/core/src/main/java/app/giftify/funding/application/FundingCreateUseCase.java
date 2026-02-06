package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.FundingCreateResult;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.application.outbound.WishlistItemSnapshotPort;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class FundingCreateUseCase {

    private final EventPublisher eventPublisher;
    private final WishlistItemSnapshotPort wishlistItemSnapshotPort;

    public List<FundingCreateResult> createFunding(List<Long> wishlistItemIds) {

        // wishlistItemId 중복 불가 -> 장바구니에 같은 wishlistItemId 담기면 금액 update로 구현해놨음
        if (wishlistItemIds.size() != new HashSet<>(wishlistItemIds).size()) {
            throw new FundingException(FundingErrorCode.DUPLICATED_WISHLIST_ITEM);
        }

        // 스냅샷 받아오기
        List<WishlistItemSnapshot> snapshots = wishlistItemSnapshotPort.getSnapshotList(wishlistItemIds);

        // 방어적 검증 : 시스템 불일치
        if (snapshots.size() != wishlistItemIds.size()) {
            throw new FundingException(FundingErrorCode.SNAPSHOT_INCONSISTENCY);
        }

        // 펀딩 생성 (단일 트랜잭션 -> 하나라도 실패 시, 전체 실패)
        List<FundingCreateResult> results = new ArrayList<>();

        for (WishlistItemSnapshot snapshot : snapshots) {
            Funding funding = Funding.startFunding(snapshot.originalWishlistItemId(), snapshot.productPrice(), snapshot.productId());
            results.add(new FundingCreateResult(funding, snapshot));

            eventPublisher.publish(new FundingCreatedEvent(
                    funding.getId(),
                    funding.getWishlistItemId()
                    // 오더아이템 아이디 추가 가넝?
            ));

            log.info("[Funding] 펀딩 생성 완료. fundingId={}", funding.getId());
        }
        return results;
    }
}
