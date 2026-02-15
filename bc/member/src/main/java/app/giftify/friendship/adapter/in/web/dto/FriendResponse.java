package app.giftify.friendship.adapter.in.web.dto;

import app.giftify.member.domain.member.Member;

public record FriendResponse(
        Long id,
        String nickname,
        String avatarUrl
) {
    public static FriendResponse from(Member member) {
        return new FriendResponse(
                member.getId(),
                member.getNickname(),
                null
        );
    }
}
