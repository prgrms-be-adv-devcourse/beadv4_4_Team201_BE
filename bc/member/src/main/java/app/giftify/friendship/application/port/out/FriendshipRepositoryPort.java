package app.giftify.friendship.application.port.out;

import java.util.List;
import java.util.Optional;
import app.giftify.friendship.domain.Friendship;
import app.giftify.shared.domain.type.FriendshipStatus;

public interface FriendshipRepositoryPort {

    Friendship save(Friendship friendship);

    Optional<Friendship> findById(Long id);

    boolean existsByMemberPairAndStatusIn(Long memberA, Long memberB, List<FriendshipStatus> statuses);

    List<Friendship> findAllByMemberIdAndStatus(Long memberId, FriendshipStatus status);

    List<Friendship> findAllByReceiverIdAndStatus(Long receiverId, FriendshipStatus status);

    void delete(Friendship friendship);
}
