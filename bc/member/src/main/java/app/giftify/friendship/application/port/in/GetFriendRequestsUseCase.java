package app.giftify.friendship.application.port.in;

import java.util.List;
import app.giftify.friendship.domain.Friendship;

public interface GetFriendRequestsUseCase {
    List<Friendship> getReceivedRequests(Long memberId);
}
