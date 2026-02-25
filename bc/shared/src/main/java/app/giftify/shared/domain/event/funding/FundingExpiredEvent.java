package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.util.List;

public class FundingExpiredEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Integer expiredAmount;
    private final Long receiverId;
    private final List<Long> participantIds;

    public FundingExpiredEvent(
            Long fundingId,
            Long wishlistItemId,
            Integer expiredAmount,
            Long receiverId,
            List<Long> participantIds
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.expiredAmount = expiredAmount;
        this.receiverId = receiverId;
        this.participantIds = List.copyOf(participantIds);
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

    public Long getReceiverId() { return receiverId; }

    public List<Long> getParticipantIds() { return participantIds; }


    @Override
    public String toString() {
        return "FundingExpiredEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", expiredAmount=" + expiredAmount +
                ", receiverId=" + receiverId +
                ", participantIds=" + participantIds +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

