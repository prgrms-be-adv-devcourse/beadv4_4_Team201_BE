package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingCanceledEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer canceledAmount;
    private final Long productId;
    private final Long fundingReceiverId;

    public FundingCanceledEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer canceledAmount,
            Long productId,
            Long fundingReceiverId
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.canceledAmount = canceledAmount;
        this.productId = productId;
        this.fundingReceiverId = fundingReceiverId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Integer getCanceledAmount() {
        return canceledAmount;
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
                ", canceledAmount=" + canceledAmount +
                ", productId=" + productId +
                ", fundingReceiverId=" + fundingReceiverId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}


