package app.giftify.friendship.adapter.in.web.dto;

import java.time.LocalDateTime;
import app.giftify.friendship.domain.Friendship;
import app.giftify.member.domain.member.Member;

public record FriendRequestResponse(
        Long friendshipId,
        FriendResponse requester,
        LocalDateTime createdAt
) {
    public static FriendRequestResponse of(Friendship friendship, Member requester) {
        return new FriendRequestResponse(
                friendship.getId(),
                FriendResponse.from(requester),
                friendship.getCreatedAt()
        );
    }
}
