package app.giftify.friendship.application.port.in;

public interface RejectFriendRequestUseCase {
    void reject(Long friendshipId, Long memberId);
}
