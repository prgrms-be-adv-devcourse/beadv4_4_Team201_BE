package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.funding.domain.event.FundingCanceledEvent;
import app.giftify.funding.domain.type.FundingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static app.giftify.funding.domain.exception.FundingErrorCode.ALREADY_TERMINATED;

@Service
@RequiredArgsConstructor
public class FundingCloseUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;

    /**
     * 펀딩 강제 종료 (관리자 전용)
     */
    public FundingCompleteResponseDto closeFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
            .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        if (funding.getStatus() == FundingStatus.CLOSED || funding.isExpired()) {
            throw new FundingException(ALREADY_TERMINATED);
        }

        funding.close();
        List<Long> participantIds = fundingParticipantMemberRepository.findIdsByFundingId((funding.getId()));

        // 환불 처리를 위한 이벤트 발행
        eventPublisher.publish(new FundingCanceledEvent(
            funding.getId(),
            funding.getWishlistItemId(),
            funding.getCurrentAmount(),
            funding.getReceiverId(),
            participantIds
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }

    /**
     * 목표 달성 후 2주 내 미 수락 종료 (스케줄러용)
     */
    public List<FundingCompleteResponseDto> closeUnacceptedAchievedFundings() {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
        List<Funding> fundings = fundingRepository.findByStatusAndAchievedAtBefore(FundingStatus.ACHIEVED, twoWeeksAgo);

        for (Funding funding : fundings) {
            funding.close();
            List<Long> participantIds = fundingParticipantMemberRepository.findIdsByFundingId((funding.getId()));

            eventPublisher.publish(new FundingCanceledEvent(
                    funding.getId(),
                    funding.getWishlistItemId(),
                    funding.getCurrentAmount(),
                    funding.getReceiverId(),
                    participantIds
            ));
        }

        return fundings.stream()
                .map(funding -> new FundingCompleteResponseDto(
                        funding.getId(),
                        funding.getWishlistItemId(),
                        funding.getProductName(),
                        funding.getStatus(),
                        funding.getClosedAt()
                ))
                .toList();
    }
}
