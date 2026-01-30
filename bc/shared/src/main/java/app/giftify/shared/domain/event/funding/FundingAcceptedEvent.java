package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.time.LocalDateTime;

public class FundingAcceptedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final LocalDateTime confirmedAt;

    public FundingAcceptedEvent(
            Long fundingId,
            Long wishlistItemId,
            LocalDateTime confirmedAt
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.confirmedAt = confirmedAt;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    @Override
    public String toString() {
        return "FundingAcceptedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", confirmedAt=" + confirmedAt +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
