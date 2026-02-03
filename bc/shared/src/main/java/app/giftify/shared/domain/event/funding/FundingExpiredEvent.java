package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingExpiredEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer expiredAmount;

    public FundingExpiredEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer expiredAmount
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.expiredAmount = expiredAmount;
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

    @Override
    public String toString() {
        return "FundingExpiredEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", expiredAmount=" + expiredAmount +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

