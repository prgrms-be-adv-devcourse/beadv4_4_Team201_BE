package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.out.FundingRepository;
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

