package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingCancelledEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer cancelledAmount;
    private final Long productId;
    private final Long fundingReceiverId;

    public FundingCancelledEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer cancelledAmount,
            Long productId,
            Long fundingReceiverId
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.cancelledAmount = cancelledAmount;
        this.productId = productId;
        this.fundingReceiverId = fundingReceiverId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Integer getCancelledAmount() {
        return cancelledAmount;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getFundingReceiverId() {
        return fundingReceiverId;
    }

    @Override
    public String toString() {
        return "FundingCancelledEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", cancelledAmount=" + cancelledAmount +
                ", productId=" + productId +
                ", fundingReceiverId=" + fundingReceiverId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

