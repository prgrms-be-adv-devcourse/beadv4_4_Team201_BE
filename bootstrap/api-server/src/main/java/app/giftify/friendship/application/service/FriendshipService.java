package app.giftify.friendship.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import app.giftify.friendship.application.port.in.*;
import app.giftify.friendship.application.port.out.FriendshipRepositoryPort;
import app.giftify.friendship.domain.Friendship;
import app.giftify.friendship.domain.FriendshipStatus;
import app.giftify.friendship.domain.exception.FriendshipErrorCode;
import app.giftify.friendship.domain.exception.FriendshipException;
import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.domain.member.Member;
import app.giftify.support.common.event.EventPublisher;
import app.giftify.friendship.domain.event.FriendshipAcceptedEvent;
import app.giftify.friendship.domain.event.FriendshipRequestSentEvent;
import lombok.RequiredArgsConstructor;
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
	private static final Logger log = LoggerFactory.getLogger(FriendshipService.class);


    private final FriendshipRepositoryPort friendshipRepository;
    // 개발 속도를 위해 같은 모듈 내 직접 참조. 별도 모듈로 분리 시 이벤트 or 내부 API로 전환 필요
    private final MemberRepositoryPort memberRepository;
    private final EventPublisher eventPublisher;

    @Override
    public Friendship sendRequest(Long requesterId, Long receiverId) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.MEMBER_NOT_FOUND,
                        "대상 회원을 찾을 수 없습니다: " + receiverId));
        if (!receiver.isActive()) {
            throw new FriendshipException(FriendshipErrorCode.WITHDRAWN_MEMBER);
        }

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
    public List<FriendInfo> getFriends(Long memberId) {
        List<Friendship> friendships = friendshipRepository.findAllByMemberIdAndStatus(
                memberId, FriendshipStatus.ACCEPTED);
        Map<Long, Long> friendIdToFriendshipId = friendships.stream()
                .collect(Collectors.toMap(
                        f -> f.getFriendId(memberId),
                        Friendship::getId));
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getFriendId(memberId))
                .toList();
        Map<Long, Member> memberMap = memberRepository.findAllByIds(friendIds).stream()
                .filter(Member::isActive)
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        return friendIds.stream()
                .filter(memberMap::containsKey)
                .map(id -> {
                    Member m = memberMap.get(id);
                    return new FriendInfo(friendIdToFriendshipId.get(id), m.getId(), m.getNickname());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestInfo> getReceivedRequests(Long memberId) {
        List<Friendship> requests = friendshipRepository.findAllByReceiverIdAndStatus(
                memberId, FriendshipStatus.PENDING);
        List<Long> requesterIds = requests.stream()
                .map(Friendship::getRequesterId)
                .toList();
        Map<Long, Member> memberMap = memberRepository.findAllByIds(requesterIds).stream()
                .filter(Member::isActive)
                .collect(Collectors.toMap(Member::getId, Function.identity()));
        return requests.stream()
                .filter(f -> memberMap.containsKey(f.getRequesterId()))
                .map(f -> {
                    Member m = memberMap.get(f.getRequesterId());
                    return new FriendRequestInfo(
                            f.getId(), m.getId(), m.getNickname(), f.getCreatedAt());
                })
                .toList();
    }

    private Friendship findFriendshipOrThrow(Long friendshipId) {
        return friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipException(FriendshipErrorCode.FRIENDSHIP_NOT_FOUND));
    }
}
