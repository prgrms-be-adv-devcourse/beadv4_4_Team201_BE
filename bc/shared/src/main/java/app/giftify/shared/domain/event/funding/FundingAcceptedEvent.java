package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.time.LocalDateTime;

public class FundingAcceptedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Long productId;
    private final LocalDateTime confirmedAt;

    public FundingAcceptedEvent(
            Long fundingId,
            Long wishlistItemId,
            Long productId,
            LocalDateTime confirmedAt
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.productId = productId;
        this.confirmedAt = confirmedAt;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Long getProductId() { return productId; }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    @Override
    public String toString() {
        return "FundingAcceptedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", productId=" + productId +
                ", confirmedAt=" + confirmedAt +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
