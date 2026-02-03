package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingCanceledEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer canceledAmount;

    public FundingCanceledEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer canceledAmount
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.canceledAmount = canceledAmount;
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


    @Override
    public String toString() {
        return "FundingCancelledEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", canceledAmount=" + canceledAmount +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}


