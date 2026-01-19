package app.giftify.in.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingStatus;

import java.time.LocalDateTime;

public record FundingResponseDto (
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
    Integer productPrice

    // TODO: 프론트를 위해 넣어주면 좋을 것 같음
    /*
    double achievementRate,  // 달성률
    Long daysRemaining     // 남은 일수
     */
) {

    public static FundingResponseDto fromEntity(Funding funding) {
        return new FundingResponseDto(
                funding.getId(),
                funding.getTargetAmount(),
                funding.getCurrentAmount(),
                funding.getStatus(),
                funding.getDeadline(),
                funding.getFundingWishlistItem().getId(),
                funding.getFundingWishlistItem().getProductId(),
                funding.getFundingWishlistItem().getProductName(),
                funding.getFundingWishlistItem().getProductPrice()
        );
    }
}
