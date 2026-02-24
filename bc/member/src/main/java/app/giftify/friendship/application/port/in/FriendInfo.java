package app.giftify.friendship.application.port.in;

public record FriendInfo(
        Long friendshipId,
        Long memberId,
        String nickname
) {
}
