package app.giftify.funding.application;

import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.funding.domain.event.FundingExpiredEvent;
import app.giftify.funding.domain.type.FundingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundingExpireUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;

    // 단일 펀딩 만료 처리 (테스트/관리자용)
    public FundingCompleteResponseDto expireFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
                .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        Integer currentAmount = funding.getCurrentAmount();

        boolean expired = funding.expire(LocalDateTime.now());

        if (expired) {
            List<Long> participantIds = fundingParticipantMemberRepository.findIdsByFundingId((funding.getId()));

            eventPublisher.publish(new FundingExpiredEvent(
                    funding.getId(),
                    funding.getWishlistItemId(),
                    currentAmount,
                    funding.getReceiverId(),
                    participantIds

            ));
        }

        return new FundingCompleteResponseDto(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getProductName(),
                funding.getStatus(),
                funding.getClosedAt()
        );
    }

    // 전체 펀딩 만료 처리 (배치/스케줄러용)
    public List<FundingCompleteResponseDto> expireExpiredFundings(LocalDateTime now) {

        //fixme: 데이터양이많아질경우
        List<Funding> expiredFundings = fundingRepository.findByDeadlineBeforeAndStatusIn(
                now,
                List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED)
        );

        for (Funding funding : expiredFundings) {
            Integer currentAmount = funding.getCurrentAmount();

            boolean expired = funding.expire(now);

            if (expired) {
                List<Long> participantIds = fundingParticipantMemberRepository.findIdsByFundingId((funding.getId()));

                eventPublisher.publish(new FundingExpiredEvent(
                        funding.getId(),
                        funding.getWishlistItemId(),
                        currentAmount,
                        funding.getReceiverId(),
                        participantIds
                ));
            }
        }

        return expiredFundings.stream()
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
