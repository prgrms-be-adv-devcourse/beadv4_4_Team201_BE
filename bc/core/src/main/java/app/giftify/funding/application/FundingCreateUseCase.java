package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.FundingCreateResult;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.application.outbound.WishlistItemSnapshotPort;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.order.domain.OrderItemSnapshot;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingDetail;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class FundingCreateUseCase {

    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;
    private final WishlistItemSnapshotPort wishlistItemSnapshotPort;

    public List<FundingCreateResult> createFunding(OrderSnapshot orderSnapshot) {

        List<OrderItemSnapshot> fundingItems = orderSnapshot.orderItemSnapshots().stream()
                .filter(item -> item.targetType() == TargetType.FUNDING_PENDING)
                .toList();

        List<Long> wishlistItemIds = fundingItems.stream()
                .map(OrderItemSnapshot::targetId)
                .toList();

        // wishlistItemId 중복 불가 -> 장바구니에 같은 wishlistItemId 담기면 금액 update로 구현해놨음
        if (wishlistItemIds.size() != new HashSet<>(wishlistItemIds).size()) {
            throw new FundingException(FundingErrorCode.DUPLICATED_WISHLIST_ITEM);
        }

        //  필요한 모든 wishlistItemId를 한번에 조회해서 Map으로 받아옴
        Map<Long, WishlistItemSnapshot> wishlistItemSnapshotMap = wishlistItemSnapshotPort.getSnapshotList(wishlistItemIds);

        // 펀딩 생성 (단일 트랜잭션 -> 하나라도 실패 시, 전체 실패)
        List<FundingCreateResult> results = new ArrayList<>();
        List<FundingDetail> fundingDetails = new ArrayList<>();

        for (OrderItemSnapshot item : fundingItems) {
            WishlistItemSnapshot wishlistItemSnapshot = wishlistItemSnapshotMap.get(item.targetId());
            
            if (wishlistItemSnapshot == null) {
                log.error("WishlistItemSnapshot을 찾을 수 없습니다. id: {}", item.targetId());
                throw new FundingException(FundingErrorCode.WISHLIST_ITEM_NOT_FOUND); // 적절한 에러 코드로 변경 필요
            }

            Funding funding = Funding.startFunding(
                    item.targetId(),                        // wishlistItemId
                    wishlistItemSnapshot.productId(),       // productId
                    wishlistItemSnapshot.productName(),     // productName
                    wishlistItemSnapshot.imageKey(),        // imageKey
                    item.receiverId(),                      // receiverId
                    item.price().amount().intValueExact()   // targetAmount
            );

            Funding savedFunding = fundingRepository.save(funding);

            results.add(new FundingCreateResult(savedFunding, item));

            fundingDetails.add(new FundingDetail(savedFunding.getId(), item.orderItemId(), savedFunding.getWishlistItemId(), item.receiverId()));

            log.info("[Funding] 펀딩 생성 완료. fundingId={}", savedFunding.getId());
        }

        eventPublisher.publish(new FundingCreatedEvent(fundingDetails));

        return results;
    }
}
