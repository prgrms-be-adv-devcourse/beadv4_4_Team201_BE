package app.giftify.friendship.domain;

import java.time.LocalDateTime;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.friendship.domain.exception.FriendshipErrorCode;
import app.giftify.support.common.base.BaseDomainModel;

public class Friendship extends BaseDomainModel {

    private final Long requesterId;
    private final Long receiverId;
    private FriendshipStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime acceptedAt;

    public Friendship(Long id, Long requesterId, Long receiverId,
                      FriendshipStatus status, LocalDateTime createdAt,
                      LocalDateTime acceptedAt) {
        super(id);
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status != null ? status : FriendshipStatus.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.acceptedAt = acceptedAt;
    }

    public static Friendship create(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new FriendshipException(FriendshipErrorCode.SELF_FRIEND_REQUEST);
        }
        return new Friendship(null, requesterId, receiverId,
                FriendshipStatus.PENDING, LocalDateTime.now(), null); // TODO : LocalDateTime 대신 파라미터 주입받기
    }

    public void accept(Long memberId) {
        validateReceiver(memberId);
        validatePending();
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject(Long memberId) {
        validateReceiver(memberId);
        validatePending();
        this.status = FriendshipStatus.REJECTED;
    }

    public void validateMember(Long memberId) {
        if (!requesterId.equals(memberId) && !receiverId.equals(memberId)) {
            throw new FriendshipException(FriendshipErrorCode.NOT_FRIENDSHIP_MEMBER);
        }
    }

    public Long getFriendId(Long memberId) {
        if (requesterId.equals(memberId)) return receiverId;
        if (receiverId.equals(memberId)) return requesterId;
        throw new FriendshipException(FriendshipErrorCode.NOT_FRIENDSHIP_MEMBER);
    }

    private void validateReceiver(Long memberId) {
        if (!receiverId.equals(memberId)) {
            throw new FriendshipException(FriendshipErrorCode.NOT_FRIENDSHIP_RECEIVER);
        }
    }

    private void validatePending() {
        if (this.status != FriendshipStatus.PENDING) {
            throw new FriendshipException(
                    FriendshipErrorCode.INVALID_FRIENDSHIP_STATUS,
                    "PENDING 상태에서만 수락/거절할 수 있습니다. 현재: " + this.status);
        }
    }

    public Long getRequesterId() { return requesterId; }
    public Long getReceiverId() { return receiverId; }
    public FriendshipStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
}
