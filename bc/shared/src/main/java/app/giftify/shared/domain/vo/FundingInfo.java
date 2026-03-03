package app.giftify.shared.domain.vo;

import app.giftify.shared.domain.type.FundingStatus;

public record FundingInfo(
        Long fundingId,
        FundingStatus status,
        Integer currentAmount,
        Integer remainingAmount     // 펀딩 모듈에서 계산
// , double achievementRate, LocalDateTime deadline
) {
}
