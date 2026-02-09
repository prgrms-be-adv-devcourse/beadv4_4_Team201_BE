package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.FundingCreateResult;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.orderDemo.domain.OrderItemSnapshot;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.type.TargetType;
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

        // 펀딩 생성 (단일 트랜잭션 -> 하나라도 실패 시, 전체 실패)
        List<FundingCreateResult> results = new ArrayList<>();

        for (OrderItemSnapshot item : fundingItems) {
            Funding funding = Funding.startFunding(item.targetId(), item.receiverId(), , // fixme : 오더스냅샷에 productId 추가?
                   , item.price().amount().intValueExact());
            results.add(new FundingCreateResult(funding, item));

            eventPublisher.publish(new FundingCreatedEvent(
                    funding.getId(),
                    funding.getWishlistItemId(),
                    item.orderItemId()
            ));

            log.info("[Funding] 펀딩 생성 완료. fundingId={}", funding.getId());
        }
        return results;
    }
}
