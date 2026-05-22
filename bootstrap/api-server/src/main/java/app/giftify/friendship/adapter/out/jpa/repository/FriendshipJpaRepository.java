package app.giftify.friendship.adapter.out.jpa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import app.giftify.friendship.adapter.out.jpa.entity.FriendshipJpaEntity;
import app.giftify.friendship.domain.FriendshipStatus;

public interface FriendshipJpaRepository extends JpaRepository<FriendshipJpaEntity, Long> {

    @Query("SELECT COUNT(f) > 0 FROM FriendshipJpaEntity f " +
            "WHERE ((f.requesterId = :memberA AND f.receiverId = :memberB) " +
            "   OR (f.requesterId = :memberB AND f.receiverId = :memberA)) " +
            "AND f.status IN :statuses")
    boolean existsByMemberPairAndStatusIn(
            @Param("memberA") Long memberA,
            @Param("memberB") Long memberB,
            @Param("statuses") List<FriendshipStatus> statuses);

    @Query("SELECT f FROM FriendshipJpaEntity f " +
            "WHERE (f.requesterId = :memberId OR f.receiverId = :memberId) " +
            "AND f.status = :status " +
            "ORDER BY f.acceptedAt DESC")
    List<FriendshipJpaEntity> findAllByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") FriendshipStatus status);

    List<FriendshipJpaEntity> findAllByReceiverIdAndStatusOrderByCreatedAtDesc(
            Long receiverId, FriendshipStatus status);
}
