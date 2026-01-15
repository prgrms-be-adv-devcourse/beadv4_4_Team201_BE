package app.funding;

import domain.funding.Funding;
import domain.funding.FundingWishlistItem;
import event.EventPublisher;
import in.event.FundingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import out.FundingRepository;
import out.FundingWishlistItemRepository;

@Service
@RequiredArgsConstructor
public class FundingCreateUseCase {
    
    private final FundingRepository fundingRepository;
    private final FundingWishlistItemRepository fundingWishlistItemRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public Funding createFunding(Long participantId, Long itemId, Integer amount) {
        FundingWishlistItem wishlistItem = fundingWishlistItemRepository.getReferenceById(itemId);
        Funding funding = Funding.startFunding(participantId, wishlistItem, amount);
        fundingRepository.save(funding);

        // Member BC에서 수신하여 WishlistItem 상태 변경 (PENDING → IN_PROGRESS)
        eventPublisher.publish(new FundingCreatedEvent(
            funding.getId(),
            wishlistItem.getWishlistId()
        ));
        
        return funding;
    }
}

