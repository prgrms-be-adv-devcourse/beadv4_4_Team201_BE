package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.FundingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FundingCloseUseCase {
    private final FundingRepository fundingRepository;

    /**
     * 펀딩 강제 종료 (사용자/관리자 요청)
     */
    public FundingCompleteResponseDto closeFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
            .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        funding.close();

        // TODO: 환불 처리 이벤트 발행
        // eventPublisher.publish(new FundingClosedEvent(funding.getId()));

        return new FundingCompleteResponseDto(
            funding.getId(),
            funding.getFundingWishlistItem().getId(),
            funding.getStatus(),
            funding.getClosedAt()
        );
    }
}
