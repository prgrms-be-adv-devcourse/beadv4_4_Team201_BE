package app.giftify.app.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingErrorCode;
import app.giftify.domain.funding.FundingException;
import app.giftify.domain.funding.FundingWishlistItem;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCanceledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        FundingWishlistItem wishlistItem = funding.getFundingWishlistItem();

        // 관리자 권한 체크 (Member BC에서 role 정보 가져와서 확인)
        // if (!isAdmin(requestMemberId)) {
        //     throw new FundingException(FundingErrorCode.FORBIDDEN, "관리자만 펀딩을 종료할 수 있습니다.");
        // }

        funding.close();

        // 환불 처리를 위한 이벤트 발행
        eventPublisher.publish(new FundingCanceledEvent(
            funding.getId(),
            wishlistItem.getWishlistId(),
            funding.getCurrentAmount(),
            wishlistItem.getProductId(),
            wishlistItem.getReceiverId()
        ));

        return FundingCompleteResponseDto.fromEntity(funding);
    }
}
