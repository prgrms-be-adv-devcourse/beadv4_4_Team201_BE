package app.giftify.funding.app.funding;

import app.giftify.funding.domain.funding.Funding;
import app.giftify.funding.domain.funding.FundingErrorCode;
import app.giftify.funding.domain.funding.FundingException;
import app.giftify.funding.domain.funding.FundingWishlistItem;
import app.giftify.funding.in.funding.FundingCompleteResponseDto;
import app.giftify.funding.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingAcceptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FundingAcceptUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public FundingCompleteResponseDto acceptFunding(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, "펀딩을 찾을 수 없습니다. ID: " + fundingId)
        );

        // 본인 펀딩인지 검증
        if (!funding.getFundingWishlistItem().getReceiverId().equals(memberId)) {
            throw new FundingException(FundingErrorCode.FORBIDDEN);
        }

        FundingWishlistItem wishlistItem = funding.getFundingWishlistItem();

        funding.accept();

        // 이벤트 발행
        eventPublisher.publish(new FundingAcceptedEvent(
                funding.getId(),
                wishlistItem.getWishlistId(),
                LocalDateTime.now()
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }
}
