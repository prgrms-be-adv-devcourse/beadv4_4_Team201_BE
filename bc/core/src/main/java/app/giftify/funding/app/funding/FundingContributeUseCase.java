package app.giftify.funding.app.funding;

import app.giftify.funding.domain.funding.Funding;
import app.giftify.funding.domain.funding.FundingErrorCode;
import app.giftify.funding.domain.funding.FundingException;
import app.giftify.funding.domain.funding.FundingWishlistItem;
import app.giftify.funding.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAchievedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingContributeUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public void contribute(Long fundingId, Integer amount) {
        Funding funding = fundingRepository.findById(fundingId)
            .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        funding.contribute(amount);
        
        // 달성된 경우 이벤트 발행
        if (funding.isAchieved()) {
            FundingWishlistItem wishlistItem = funding.getFundingWishlistItem();
            
            eventPublisher.publish(new FundingAchievedEvent(
                funding.getId(),
                wishlistItem.getWishlistId(),
                funding.getTargetAmount(),
                wishlistItem.getProductId(),
                wishlistItem.getReceiverId()
            ));
        }
    }
}
