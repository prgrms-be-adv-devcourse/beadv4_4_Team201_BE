package app.giftify.funding.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingParticipantMemberRepository;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.funding.adapter.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.funding.domain.event.FundingCanceledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FundingRefuseUseCase {
	private static final Logger log = LoggerFactory.getLogger(FundingRefuseUseCase.class);

    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;
    private final FundingParticipantMemberRepository fundingParticipantMemberRepository;

    public FundingCompleteResponseDto refuseFunding(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, "펀딩을 찾을 수 없습니다. ID: " + fundingId)
        );

        if (!memberId.equals(funding.getReceiverId())) { throw new FundingException(FundingErrorCode.FORBIDDEN); }

        // 거절로 상태 변경
        funding.refuse();
        List<Long> participantIds = fundingParticipantMemberRepository.findIdsByFundingId((funding.getId()));

        log.info("[Funding] 펀딩 거절 완료" + fundingId);

        // 이벤트 발행
        eventPublisher.publish(new FundingCanceledEvent(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getCurrentAmount(),
                funding.getReceiverId(),
                participantIds
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }


    }
