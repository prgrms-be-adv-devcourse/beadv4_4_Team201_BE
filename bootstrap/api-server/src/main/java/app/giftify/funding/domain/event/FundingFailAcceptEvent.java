package app.giftify.funding.domain.event;

public record FundingFailAcceptEvent (Long fundingId, Long receiverId) { }
