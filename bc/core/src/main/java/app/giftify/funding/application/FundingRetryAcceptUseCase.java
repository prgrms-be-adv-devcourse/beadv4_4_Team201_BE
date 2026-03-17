package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FundingRetryAcceptUseCase {
    private final FundingRepository fundingRepository;

    public FundingCompleteResponseDto retryAccept(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId)
                .orElseThrow(()-> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, fundingId));

        funding.validateReceiver(memberId);

        LocalDateTime now = LocalDateTime.now();

        if (!funding.canRetryAccept(now)) {
            throw new FundingException(FundingErrorCode.INVALID_STATUS_FOR_RETRY_ACCEPT, fundingId);
        }

        funding.pendingAcceptance();
        return FundingCompleteResponseDto.fromEntity(funding);
    }
}
