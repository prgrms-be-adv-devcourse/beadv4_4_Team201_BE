package app.giftify.in.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingStatus;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MyFundingResponseDto(
        // 펀딩 정보
        Long fundingId,
        Integer targetAmount,
        Integer currentAmount,
        FundingStatus status,
        LocalDateTime deadline,

        // 위시리스트 아이템 정보
        Long wishlistItemId,
        Long productId,
        String productName,
        Integer productPrice,

        // 추가 정보
        double achievementRate,  // 달성률 (%)
        long daysRemaining,      // 남은 일수

        // 나의 기여금 정보
        Integer myContribution
) {

    public static MyFundingResponseDto fromEntity(Funding funding, Integer myContribution) {
        double rate = 0.0;
        if (funding.getTargetAmount() > 0) {
            rate = (double) funding.getCurrentAmount() / funding.getTargetAmount() * 100.0;
        }

        long days = 0;
        if (funding.getDeadline() != null) {
            days = ChronoUnit.DAYS.between(LocalDateTime.now(), funding.getDeadline());
            if (days < 0) days = 0;
        }

        return new MyFundingResponseDto(
                funding.getId(),
                funding.getTargetAmount(),
                funding.getCurrentAmount(),
                funding.getStatus(),
                funding.getDeadline(),
                funding.getFundingWishlistItem().getId(),
                funding.getFundingWishlistItem().getProductId(),
                funding.getFundingWishlistItem().getProductName(),
                funding.getFundingWishlistItem().getProductPrice(),
                Math.round(rate * 10.0) / 10.0,
                days,
                myContribution
        );
    }
}
