package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingExpiredEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer expiredAmount;
    private final Long fundingReceiverId;

    public FundingExpiredEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer expiredAmount,
            Long fundingReceiverId
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.expiredAmount = expiredAmount;
        this.fundingReceiverId = fundingReceiverId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Integer getExpiredAmount() {
        return expiredAmount;
    }

    public Long getFundingReceiverId() {
        return fundingReceiverId;
    }

    @Override
    public String toString() {
        return "FundingExpiredEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", expiredAmount=" + expiredAmount +
                ", fundingReceiverId=" + fundingReceiverId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

