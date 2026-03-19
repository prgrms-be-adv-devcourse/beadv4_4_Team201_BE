package app.giftify.shared.domain.event.funding;

public record FundingFailAcceptEvent (Long fundingId, Long receiverId) { }
