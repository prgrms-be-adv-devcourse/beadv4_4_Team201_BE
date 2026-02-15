package app.giftify.friendship.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.giftify.friendship.application.port.in.*;
import app.giftify.friendship.application.port.out.FriendshipRepositoryPort;
import app.giftify.friendship.domain.Friendship;
import app.giftify.friendship.domain.FriendshipStatus;
import app.giftify.friendship.domain.exception.FriendshipErrorCode;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.friendship.FriendshipAcceptedEvent;
import app.giftify.shared.domain.event.friendship.FriendshipRequestSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService implements
        SendFriendRequestUseCase,
        AcceptFriendRequestUseCase,
        RejectFriendRequestUseCase,
        RemoveFriendUseCase,
        GetFriendListUseCase,
        GetFriendRequestsUseCase {

    private final FriendshipRepositoryPort friendshipRepository;
    private final MemberRepositoryPort memberRepository;
    private final EventPublisher eventPublisher;

    @Override
    public Friendship sendRequest(Long requesterId, Long receiverId) {
        memberRepository.findById(receiverId)
                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.MEMBER_NOT_FOUND,
                        "대상 회원을 찾을 수 없습니다: " + receiverId));

        boolean exists = friendshipRepository.existsByMemberPairAndStatusIn(
                requesterId, receiverId,
                List.of(FriendshipStatus.PENDING, FriendshipStatus.ACCEPTED));
        if (exists) {
            throw new FriendshipException(FriendshipErrorCode.DUPLICATE_FRIENDSHIP);
        }

        Friendship friendship = Friendship.create(requesterId, receiverId);
        Friendship saved = friendshipRepository.save(friendship);

        eventPublisher.publish(
                new FriendshipRequestSentEvent(saved.getId(), requesterId, receiverId));
        log.info("[FriendshipService] 친구 요청 전송: {} -> {}", requesterId, receiverId);

        return saved;
    }

    @Override
    public Friendship accept(Long friendshipId, Long memberId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);
        friendship.accept(memberId);
        Friendship saved = friendshipRepository.save(friendship);

        eventPublisher.publish(
                new FriendshipAcceptedEvent(saved.getId(), saved.getRequesterId(), saved.getReceiverId()));
        log.info("[FriendshipService] 친구 요청 수락: friendshipId={}", friendshipId);

        return saved;
    }

    @Override
    public void reject(Long friendshipId, Long memberId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);
        friendship.reject(memberId);
        friendshipRepository.save(friendship);
        log.info("[FriendshipService] 친구 요청 거절: friendshipId={}", friendshipId);
    }

    @Override
    public void remove(Long friendshipId, Long memberId) {
        Friendship friendship = findFriendshipOrThrow(friendshipId);
        friendship.validateMember(memberId);
        friendshipRepository.delete(friendship);
        log.info("[FriendshipService] 친구 삭제: friendshipId={}", friendshipId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> getFriends(Long memberId) {
        return friendshipRepository.findAllByMemberIdAndStatus(memberId, FriendshipStatus.ACCEPTED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Friendship> getReceivedRequests(Long memberId) {
        return friendshipRepository.findAllByReceiverIdAndStatus(memberId, FriendshipStatus.PENDING);
    }

    private Friendship findFriendshipOrThrow(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));
    }
}
