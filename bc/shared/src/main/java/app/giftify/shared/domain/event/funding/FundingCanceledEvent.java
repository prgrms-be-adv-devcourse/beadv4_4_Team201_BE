package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.util.List;

public class FundingCanceledEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer canceledAmount;
    private final Long receiverId;
    private final List<Long> participantIds;

    public FundingCanceledEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer canceledAmount,
            Long receiverId,
            List<Long> participantIds
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.canceledAmount = canceledAmount;
        this.receiverId = receiverId;
        this.participantIds = List.copyOf(participantIds);
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

    public Long getReceiverId() { return receiverId; }

    public List<Long> getParticipantIds() { return participantIds; }


    @Override
    public String toString() {
        return "FundingCancelledEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", canceledAmount=" + canceledAmount +
                ", receiverId=" + receiverId +
                ", participantIds=" + participantIds +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}


