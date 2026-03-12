package app.giftify.friendship.adapter.out.jpa.entity;

import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import app.giftify.friendship.domain.FriendshipStatus;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FriendshipJpaEntity extends BaseJpaEntity {

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    public FriendshipJpaEntity(Long id, Long requesterId, Long receiverId,
                                FriendshipStatus status, LocalDateTime acceptedAt) {
        super(id);
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
        this.acceptedAt = acceptedAt;
    }
}
