package app.giftify.friendship.adapter.out.jpa.adapter;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import app.giftify.friendship.adapter.out.jpa.entity.FriendshipJpaEntity;
import app.giftify.friendship.adapter.out.jpa.mapper.FriendshipMapper;
import app.giftify.friendship.adapter.out.jpa.repository.FriendshipJpaRepository;
import app.giftify.friendship.application.port.out.FriendshipRepositoryPort;
import app.giftify.friendship.domain.Friendship;
import app.giftify.shared.domain.type.FriendshipStatus;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FriendshipPersistenceAdapter implements FriendshipRepositoryPort {

    private final FriendshipJpaRepository repository;

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipJpaEntity entity = FriendshipMapper.toEntity(friendship);
        FriendshipJpaEntity saved = repository.save(entity);
        return FriendshipMapper.toDomain(saved);
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        return repository.findById(id).map(FriendshipMapper::toDomain);
    }

    @Override
    public boolean existsByMemberPairAndStatusIn(Long memberA, Long memberB,
                                                  List<FriendshipStatus> statuses) {
        return repository.existsByMemberPairAndStatusIn(memberA, memberB, statuses);
    }

    @Override
    public List<Friendship> findAllByMemberIdAndStatus(Long memberId, FriendshipStatus status) {
        return repository.findAllByMemberIdAndStatus(memberId, status)
                .stream().map(FriendshipMapper::toDomain).toList();
    }

    @Override
    public List<Friendship> findAllByReceiverIdAndStatus(Long receiverId, FriendshipStatus status) {
        return repository.findAllByReceiverIdAndStatusOrderByCreatedAtDesc(receiverId, status)
                .stream().map(FriendshipMapper::toDomain).toList();
    }

    @Override
    public void delete(Friendship friendship) {
        repository.deleteById(friendship.getId());
    }
}
