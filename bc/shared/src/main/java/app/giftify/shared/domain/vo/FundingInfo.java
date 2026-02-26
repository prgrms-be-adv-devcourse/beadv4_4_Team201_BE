package app.giftify.shared.domain.vo;

public record FundingInfo(
        Long wishlistItemId,
        Integer currentAmount,
        Integer remainingAmount     // 펀딩 모듈에서 계산
// , double achievementRate, LocalDateTime deadline
) {
}
