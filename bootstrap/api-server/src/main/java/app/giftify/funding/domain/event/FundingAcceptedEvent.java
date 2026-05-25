package app.giftify.funding.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;

import java.time.LocalDateTime;

public class FundingAcceptedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Long productId;
    private final Long receiverId;
    private final LocalDateTime confirmedAt;

    public FundingAcceptedEvent(
            Long fundingId,
            Long wishlistItemId,
            Long productId,
            Long receiverId,
            LocalDateTime confirmedAt
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.productId = productId;
        this.receiverId = receiverId;
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

    public Long getReceiverId() { return receiverId; }

    @Override
    public String toString() {
        return "FundingAcceptedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", productId=" + productId +
                ", receiverId=" + receiverId +
                ", confirmedAt=" + confirmedAt +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}
