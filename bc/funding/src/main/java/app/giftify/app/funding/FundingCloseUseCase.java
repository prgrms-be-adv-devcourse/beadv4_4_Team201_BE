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
     * 펀딩 강제 종료 (관리자 전용)
     */
    public FundingCompleteResponseDto closeFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
            .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        // 관리자 권한 체크 (Member BC에서 role 정보 가져와서 확인)
        // if (!isAdmin(requestMemberId)) {
        //     throw new FundingException(FundingErrorCode.FORBIDDEN, "관리자만 펀딩을 종료할 수 있습니다.");
        // }

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
