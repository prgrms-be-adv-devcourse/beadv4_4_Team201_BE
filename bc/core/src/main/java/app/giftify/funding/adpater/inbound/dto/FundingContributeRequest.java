package app.giftify.funding.adpater.inbound.dto;

public record FundingContributeRequest(
        Long fundingId,
        Integer amount
) {
}
