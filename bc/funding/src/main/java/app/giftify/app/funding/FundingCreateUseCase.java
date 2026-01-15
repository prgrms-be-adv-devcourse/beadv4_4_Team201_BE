package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.support.common.event.funding.FundingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.giftify.out.FundingRepository;
import app.giftify.out.FundingWishlistItemRepository;

@Service
@RequiredArgsConstructor
public class FundingCreateUseCase {
    
    private final FundingRepository fundingRepository;
    private final FundingWishlistItemRepository fundingWishlistItemRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public Funding createFunding(Long itemId, Integer amount) {
        FundingWishlistItem wishlistItem = fundingWishlistItemRepository.getReferenceById(itemId);
        Funding funding = Funding.startFunding(wishlistItem, amount);
        fundingRepository.save(funding);

        // Member BC에서 수신하여 WishlistItem 상태 변경 (PENDING → IN_PROGRESS)
        eventPublisher.publish(new FundingCreatedEvent(
            funding.getId(),
            wishlistItem.getWishlistId()
        ));
        
        return funding;
    }
}
