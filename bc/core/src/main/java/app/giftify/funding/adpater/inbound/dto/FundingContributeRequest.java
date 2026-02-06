package app.giftify.funding.adpater.inbound.dto;

import app.giftify.shared.domain.vo.Money;

public record FundingContributeRequest(
        Long fundingId,
        Integer amount
) {
}
