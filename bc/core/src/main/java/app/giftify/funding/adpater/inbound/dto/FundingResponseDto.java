package app.giftify.funding.adpater.inbound.dto;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.FundingStatus;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record FundingResponseDto (
    // 펀딩 정보
    Long fundingId,
    Integer targetAmount,
    Integer currentAmount,
    FundingStatus status,
    LocalDateTime deadline,

    // 위시리스트 아이템 정보
    Long wishlistItemId,
    String productName,
    Integer productPrice,

    // 추가 정보 -> 화면 출력용
    double achievementRate,  // 달성률 (%)
    long daysRemaining       // 남은 일수
) {

    public static FundingResponseDto fromEntity(Funding funding, WishlistItemSnapshot snapshot) {
        double rate = 0.0;
        if (funding.getTargetAmount() > 0) {
            rate = (double) funding.getCurrentAmount() / funding.getTargetAmount() * 100.0;
        }

        long days = 0;
        if (funding.getDeadline() != null) {
            days = ChronoUnit.DAYS.between(LocalDateTime.now(), funding.getDeadline());
            if (days < 0) days = 0;
        }

        return new FundingResponseDto(
                funding.getId(),
                funding.getTargetAmount(),
                funding.getCurrentAmount(),
                funding.getStatus(),
                funding.getDeadline(),
                snapshot.originalWishlistItemId(),
                snapshot.productName(),
                snapshot.productPrice(),
                Math.round(rate * 10.0) / 10.0, // 소수점 첫째자리까지 반올림
                days
        );
    }
}
