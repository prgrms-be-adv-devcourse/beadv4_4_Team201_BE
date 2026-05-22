package app.giftify.friendship.application.port.in;

import app.giftify.friendship.domain.Friendship;

public interface AcceptFriendRequestUseCase {
    Friendship accept(Long friendshipId, Long memberId);
}
