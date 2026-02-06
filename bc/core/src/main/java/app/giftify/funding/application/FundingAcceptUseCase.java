package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
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

        if (!memberId.equals(funding.getReceiverId())) { throw new FundingException(FundingErrorCode.FORBIDDEN); }

        funding.accept();

        // 이벤트 발행
        eventPublisher.publish(new FundingAcceptedEvent(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getProductId(),
                funding.getReceiverId(),
                LocalDateTime.now()
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }
}
