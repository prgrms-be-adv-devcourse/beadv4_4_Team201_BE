package app.giftify.friendship.application.port.in;

import app.giftify.friendship.domain.Friendship;

public interface SendFriendRequestUseCase {
    Friendship sendRequest(Long requesterId, Long receiverId);
}
