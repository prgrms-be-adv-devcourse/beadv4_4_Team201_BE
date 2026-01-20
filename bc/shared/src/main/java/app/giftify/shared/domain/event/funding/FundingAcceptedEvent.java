package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingAcceptedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;

    public FundingAcceptedEvent(
            Long fundingId,
            Long wishlistItemId
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
    }

    public Long getFundingId() {
        return fundingId;
    }
    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    @Override
    public String toString() {
        return "FundingAcceptedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
