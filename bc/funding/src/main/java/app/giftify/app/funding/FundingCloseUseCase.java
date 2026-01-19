package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingCloseUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public FundingCompleteResponseDto closeFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
            .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        FundingWishlistItem wishlistItem = funding.getFundingWishlistItem();
        Integer currentAmount = funding.getCurrentAmount();
        
        funding.close();

        // 환불 처리를 위한 이벤트 발행
        eventPublisher.publish(new FundingCanceledEvent(
            funding.getId(),
            wishlistItem.getWishlistId(),
            currentAmount,
            wishlistItem.getProductId(),
            wishlistItem.getReceiverId()
        ));

        return new FundingCompleteResponseDto(
            funding.getId(),
            funding.getFundingWishlistItem().getId(),
            funding.getStatus(),
            funding.getClosedAt()
        );
    }
}
