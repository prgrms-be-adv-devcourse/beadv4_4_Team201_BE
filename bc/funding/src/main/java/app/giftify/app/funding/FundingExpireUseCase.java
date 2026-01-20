package app.giftify.app.funding;

import app.giftify.domain.funding.*;
import app.giftify.in.funding.FundingCompleteResponseDto;
import app.giftify.out.funding.FundingRepository;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingExpiredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FundingExpireUseCase {
    private final FundingRepository fundingRepository;
    private final EventPublisher eventPublisher;

    // 단일 펀딩 만료 처리 (테스트/관리자용)
    public FundingCompleteResponseDto expireFunding(Long id) {
        Funding funding = fundingRepository.findById(id)
                .orElseThrow(() -> new FundingException(FundingErrorCode.FUNDING_NOT_FOUND));

        // 관리자 권한 체크 (Member BC에서 role 정보 가져와서 확인)
        // if (!isAdmin(requestMemberId)) {
        //     throw new FundingException(FundingErrorCode.FORBIDDEN, "관리자만 펀딩을 종료할 수 있습니다.");
        // }

        FundingWishlistItem wishlistItem = funding.getFundingWishlistItem();
        Integer currentAmount = funding.getCurrentAmount();


        funding.expire();

        eventPublisher.publish(new FundingExpiredEvent(
                funding.getId(),
                wishlistItem.getWishlistId(),
                currentAmount,
                wishlistItem.getReceiverId()
        ));

        return new FundingCompleteResponseDto(
                funding.getId(),
                funding.getFundingWishlistItem().getId(),
                funding.getStatus(),
                funding.getClosedAt()
        );
    }

    // 전체 펀딩 만료 처리 (배치/스케줄러용)
    public List<FundingCompleteResponseDto> expireExpiredFundings() {
        LocalDateTime now = LocalDateTime.now();

        //fixme: 데이터양이많아질경우
        List<Funding> expiredFundings = fundingRepository.findByDeadlineAfterAndStatusIn(
                now,
                List.of(FundingStatus.IN_PROGRESS, FundingStatus.ACHIEVED)
        );

        for (Funding funding : expiredFundings) {
            FundingWishlistItem wishlistItem = funding.getFundingWishlistItem();
            Integer currentAmount = funding.getCurrentAmount();

            funding.expire();

            eventPublisher.publish(new FundingExpiredEvent(
                    funding.getId(),
                    wishlistItem.getWishlistId(),
                    currentAmount,
                    wishlistItem.getReceiverId()
            ));
        }

        return expiredFundings.stream()
                .map(funding -> new FundingCompleteResponseDto(
                        funding.getId(),
                        funding.getFundingWishlistItem().getId(),
                        funding.getStatus(),
                        funding.getClosedAt()
                ))
                .toList();
    }
}