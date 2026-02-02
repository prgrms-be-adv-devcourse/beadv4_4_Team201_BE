package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class FundingCreateUseCase {

    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public Funding createFunding(Long wishlistItemId) {
        WishlistItemSnapshot snapshot = wishlistItemSnapshotPort.getSnapshot(wishlistItemId);
        Integer targetAmount = snapshot.productrPrice();

        Funding funding = Funding.startFunding(wishlistItemId, targetAmount);
        fundingRepository.save(funding);

        // WishlistItem 상태 변경 (PENDING → IN_PROGRESS)
        eventPublisher.publish(new FundingCreatedEvent(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getDeadline()
        ));

        log.info("[Funding] 펀딩 생성 완료. fundingId={}", funding.getId());
        return funding;
    }
}
