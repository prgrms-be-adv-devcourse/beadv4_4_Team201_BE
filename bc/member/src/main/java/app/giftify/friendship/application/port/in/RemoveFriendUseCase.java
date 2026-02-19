package app.giftify.friendship.application.port.in;

public interface RemoveFriendUseCase {
    void remove(Long friendshipId, Long memberId);
}
