package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundingRefuseUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    public FundingCompleteResponseDto refuseFunding(Long fundingId, Long memberId) {
        Funding funding = fundingRepository.findById(fundingId).orElseThrow(() ->
                new FundingException(FundingErrorCode.FUNDING_NOT_FOUND, "펀딩을 찾을 수 없습니다. ID: " + fundingId)
        );

        if (!memberId.equals(funding.getReceiverId())) { throw new FundingException(FundingErrorCode.FORBIDDEN); }

        // 거절로 상태 변경
        funding.refuse();
        log.info("[Funding] 펀딩 거절 완료" + fundingId);

        // 이벤트 발행
        eventPublisher.publish(new FundingCanceledEvent(
                funding.getId(),
                funding.getWishlistItemId(),
                funding.getCurrentAmount()
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }


    }
