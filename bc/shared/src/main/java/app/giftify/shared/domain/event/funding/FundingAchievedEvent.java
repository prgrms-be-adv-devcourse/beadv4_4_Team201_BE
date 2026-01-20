package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FundingAchievedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer achievedAmount;
    private final Long productId;
    private final Long fundingReceiverId;

    public FundingAchievedEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer achievedAmount,
            Long productId,
            Long fundingReceiverId
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.achievedAmount = achievedAmount;
        this.productId = productId;
        this.fundingReceiverId = fundingReceiverId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Integer getAchievedAmount() {
        return achievedAmount;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getFundingReceiverId() {
        return fundingReceiverId;
    }

    @Override
    public String toString() {
        return "FundingAchievedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", achievedAmount=" + achievedAmount +
                ", productId=" + productId +
                ", fundingReceiverId=" + fundingReceiverId +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

