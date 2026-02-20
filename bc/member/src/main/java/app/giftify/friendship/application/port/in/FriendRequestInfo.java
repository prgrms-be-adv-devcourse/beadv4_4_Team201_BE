package app.giftify.friendship.application.port.in;

import java.time.LocalDateTime;

public record FriendRequestInfo(
        Long friendshipId,
        Long requesterId,
        String requesterNickname,
        LocalDateTime createdAt
) {
}
