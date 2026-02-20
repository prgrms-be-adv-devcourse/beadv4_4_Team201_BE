package app.giftify.friendship.adapter.out.jpa.adapter;

import app.giftify.friendship.adapter.out.jpa.repository.FriendshipJpaRepository;
import app.giftify.shared.domain.port.FriendshipVerificationPort;
import app.giftify.shared.domain.type.FriendshipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
}
