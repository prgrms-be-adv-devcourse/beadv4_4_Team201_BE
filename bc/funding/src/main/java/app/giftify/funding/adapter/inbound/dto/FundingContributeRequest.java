package app.giftify.funding.adapter.inbound.dto;

public record FundingContributeRequest(
        Long fundingId,
        Integer amount
) {
}
