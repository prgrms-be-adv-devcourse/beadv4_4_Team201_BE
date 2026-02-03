package app.giftify.shared.domain.vo;

import app.giftify.shared.domain.type.FundingStatus;

public record FundingSnapshot(
        Long fundingId,
        FundingStatus status
) {
}
