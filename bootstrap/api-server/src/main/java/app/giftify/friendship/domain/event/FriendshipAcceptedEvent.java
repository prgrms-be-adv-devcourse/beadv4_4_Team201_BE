package app.giftify.friendship.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;

public class FriendshipAcceptedEvent extends BaseDomainEvent {
    private final Long friendshipId;
    private final Long requesterId;
    private final Long receiverId;

    public FriendshipAcceptedEvent(Long friendshipId, Long requesterId, Long receiverId) {
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
        return "FriendshipAcceptedEvent{friendshipId=" + friendshipId +
                ", requesterId=" + requesterId + ", receiverId=" + receiverId + "}";
    }
}
