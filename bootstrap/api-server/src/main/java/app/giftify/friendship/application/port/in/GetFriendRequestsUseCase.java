package app.giftify.friendship.application.port.in;

import java.util.List;

public interface GetFriendRequestsUseCase {
    List<FriendRequestInfo> getReceivedRequests(Long memberId);
}
