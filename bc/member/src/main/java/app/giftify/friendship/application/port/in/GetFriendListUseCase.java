package app.giftify.friendship.application.port.in;

import java.util.List;
import app.giftify.friendship.domain.Friendship;

public interface GetFriendListUseCase {
    List<Friendship> getFriends(Long memberId);
}
