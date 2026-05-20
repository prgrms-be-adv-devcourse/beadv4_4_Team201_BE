package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingConfirmPendingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundingAcceptUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public FundingCompleteResponseDto requestFundingAcceptance(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, fundingId)
        );

        funding.validateReceiver(memberId);
        funding.pendingAcceptance();
        log.info("[Funding] 펀딩 수락 확정 대기 시작. fundingId={}", fundingId);

        eventPublisher.publish(new FundingConfirmPendingEvent(
                funding.getId(),
                funding.getProductId()
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }

    public void confirmFundingAcceptance(Long fundingId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, fundingId)
        );

        funding.confirmAcceptance();
        log.info("[Funding] 펀딩 수락 확정 완료. fundingId={}", fundingId);
    }

}
