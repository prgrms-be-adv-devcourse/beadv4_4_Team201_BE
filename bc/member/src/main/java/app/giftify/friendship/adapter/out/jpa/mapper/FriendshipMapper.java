package app.giftify.friendship.adapter.out.jpa.mapper;

import app.giftify.friendship.adapter.out.jpa.entity.FriendshipJpaEntity;
import app.giftify.friendship.domain.Friendship;

public class FriendshipMapper {

    public static Friendship toDomain(FriendshipJpaEntity entity) {
        if (entity == null) return null;
        return new Friendship(
                entity.getId(),
                entity.getRequesterId(),
                entity.getReceiverId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getAcceptedAt()
        );
    }

    public static FriendshipJpaEntity toEntity(Friendship domain) {
        if (domain == null) return null;
        return FriendshipJpaEntity.builder()
                .id(domain.getId())
                .requesterId(domain.getRequesterId())
                .receiverId(domain.getReceiverId())
                .status(domain.getStatus())
                .acceptedAt(domain.getAcceptedAt())
                .build();
    }
}
