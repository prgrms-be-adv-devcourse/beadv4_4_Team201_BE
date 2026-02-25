package app.giftify.shared.domain.vo;

public record FundingInfo(
        Long wishlistItemId, Integer currentAmount
//, Integer targetAmount, double achievementRate, LocalDateTime deadline
) {
}
