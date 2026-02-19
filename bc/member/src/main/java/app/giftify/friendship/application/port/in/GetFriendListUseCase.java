package app.giftify.friendship.application.port.in;

import java.util.List;

public interface GetFriendListUseCase {
    List<FriendInfo> getFriends(Long memberId);
}
