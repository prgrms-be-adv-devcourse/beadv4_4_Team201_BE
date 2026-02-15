package app.giftify.shared.domain.event.friendship;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class FriendshipRequestSentEvent extends BaseDomainEvent {
    private final Long friendshipId;
    private final Long requesterId;
    private final Long receiverId;

    public FriendshipRequestSentEvent(Long friendshipId, Long requesterId, Long receiverId) {
        super();
        this.friendshipId = friendshipId;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
    }

    public Long getFriendshipId() { return friendshipId; }
    public Long getRequesterId() { return requesterId; }
    public Long getReceiverId() { return receiverId; }

    @Override
    public String toString() {
        return "FriendshipRequestSentEvent{friendshipId=" + friendshipId +
                ", requesterId=" + requesterId + ", receiverId=" + receiverId + "}";
    }
}
