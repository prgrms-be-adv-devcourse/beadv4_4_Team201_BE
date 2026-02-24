package app.giftify.friendship.adapter.in.web.dto;

import app.giftify.friendship.application.port.in.FriendInfo;

public record FriendResponse(
        Long friendshipId,
        Long id,
        String nickname,
        String avatarUrl
) {
    public static FriendResponse from(FriendInfo info) {
        return new FriendResponse(
                info.friendshipId(),
                info.memberId(),
                info.nickname(),
                null
        );
    }
}
