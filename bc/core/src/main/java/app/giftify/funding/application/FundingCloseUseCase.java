package app.giftify.funding.application;

import app.giftify.funding.adpater.inbound.dto.FundingCompleteResponseDto;
import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import app.giftify.funding.domain.FundingStatus;
import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
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

        // 환불 처리를 위한 이벤트 발행
        eventPublisher.publish(new FundingCanceledEvent(
            funding.getId(),
            funding.getWishlistItemId(),
            funding.getCurrentAmount()
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }

    /**
     * 목표 달성 펀딩 2주내 미수락 시 종료 (스케줄러용)
     */
    public List<FundingCompleteResponseDto> closeUnacceptedAchievedFundings(LocalDateTime now) {
//        펀딩 상태가 "목표 달성" 상태인지 확인

//        이미 수락/종료되었는지 확인
        List<Funding> achievedFundings = fundingRepository.findAchievedFundingsBefore(now);
    }
}
