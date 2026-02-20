package app.giftify.friendship.adapter.in.web.dto;

import app.giftify.friendship.application.port.in.FriendInfo;

public record FriendResponse(
        Long id,
        String nickname,
        String avatarUrl
) {
    public static FriendResponse from(FriendInfo info) {
        return new FriendResponse(
                info.memberId(),
                info.nickname(),
                null
        );
    }
}
