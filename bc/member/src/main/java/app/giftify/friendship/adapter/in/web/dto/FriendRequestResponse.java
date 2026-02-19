package app.giftify.friendship.adapter.in.web.dto;

import java.time.LocalDateTime;
import app.giftify.friendship.application.port.in.FriendRequestInfo;

public record FriendRequestResponse(
        Long friendshipId,
        FriendResponse requester,
        LocalDateTime createdAt
) {
    public static FriendRequestResponse from(FriendRequestInfo info) {
        return new FriendRequestResponse(
                info.friendshipId(),
                new FriendResponse(info.requesterId(), info.requesterNickname(), null),
                info.createdAt()
        );
    }
}
