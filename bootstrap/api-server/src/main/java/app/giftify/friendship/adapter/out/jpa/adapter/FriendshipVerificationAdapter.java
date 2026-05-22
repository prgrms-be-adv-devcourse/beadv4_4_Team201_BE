package app.giftify.friendship.adapter.out.jpa.adapter;

import app.giftify.friendship.adapter.out.jpa.entity.FriendshipJpaEntity;
import app.giftify.friendship.adapter.out.jpa.repository.FriendshipJpaRepository;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import app.giftify.friendship.domain.FriendshipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendshipVerificationAdapter implements FriendshipVerificationPort {

    private final FriendshipJpaRepository friendshipJpaRepository;

    @Override
    public boolean areFriends(Long memberId, Long friendId) {
        return friendshipJpaRepository.existsByMemberPairAndStatusIn(
                memberId,
                friendId,
                List.of(FriendshipStatus.ACCEPTED)
        );
    }

    @Override
    public List<Long> getFriendIds(Long memberId) {
        List<FriendshipJpaEntity> friendships = friendshipJpaRepository.findAllByMemberIdAndStatus(
                memberId,
                FriendshipStatus.ACCEPTED
        );

        return friendships.stream()
                .map(friendship -> friendship.getRequesterId().equals(memberId)
                        ? friendship.getReceiverId()
                        : friendship.getRequesterId())
                .collect(Collectors.toList());
    }
}
