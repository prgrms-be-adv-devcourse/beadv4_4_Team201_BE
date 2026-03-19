package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingConfirmPendingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FundingRetryAcceptUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public FundingCompleteResponseDto retryAccept(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(()-> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, fundingId));

        funding.validateReceiver(memberId);

        LocalDateTime now = LocalDateTime.now();

        if (!funding.canRetryAccept(now)) {
            throw new FundingException(FundingErrorCode.INVALID_STATUS_FOR_RETRY_ACCEPT, fundingId);
        }

        funding.pendingAcceptance();
        eventPublisher.publish(new FundingConfirmPendingEvent(funding.getId(), funding.getProductId()));

        return FundingCompleteResponseDto.fromEntity(funding);
    }
}
