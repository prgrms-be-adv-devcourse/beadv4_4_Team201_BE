package app.giftify.friendship.adapter.in.web.dto;

import java.time.LocalDateTime;
import app.giftify.friendship.domain.Friendship;
import app.giftify.shared.domain.type.FriendshipStatus;

public record FriendshipResponse(
        Long id,
        Long requesterId,
        Long receiverId,
        FriendshipStatus status,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt
) {
    public static FriendshipResponse from(Friendship friendship) {
        return new FriendshipResponse(
                friendship.getId(),
                friendship.getRequesterId(),
                friendship.getReceiverId(),
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getAcceptedAt()
        );
    }
}
