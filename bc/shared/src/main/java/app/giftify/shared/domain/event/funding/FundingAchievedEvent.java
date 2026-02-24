package app.giftify.shared.domain.event.funding;

import app.giftify.shared.domain.event.BaseDomainEvent;

import java.util.List;

public class FundingAchievedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long wishlistItemId;
    private final Long receiverId;
    private final List<Long> participantIds;


    public FundingAchievedEvent(
            Long fundingId,
            Long wishlistItemId,
            Long receiverId,
            List<Long> participantIds
    ) {
        super();
        this.fundingId = fundingId;
        this.wishlistItemId = wishlistItemId;
        this.receiverId = receiverId;
        this.participantIds = List.copyOf(participantIds);
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getWishlistItemId() {
        return wishlistItemId;
    }

    public Long getReceiverId() { return receiverId; }

    public List<Long> getParticipantIds() { return participantIds; }



    @Override
    public String toString() {
        return "FundingAchievedEvent{" +
                "fundingId=" + fundingId +
                ", wishlistItemId=" + wishlistItemId +
                ", receiverId=" + receiverId +
                ", participantIds=" + participantIds +
                ", eventId='" + getEventId() + "'" +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}

