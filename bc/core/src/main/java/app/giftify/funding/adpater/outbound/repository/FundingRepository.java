package app.giftify.funding.adpater.outbound.repository;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.domain.FundingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Funding save(Funding funding);

    Page<Funding> findAllByStatusIn(List<FundingStatus> statuses, Pageable pageable);

    List<Funding> findByDeadlineAfterAndStatusIn(LocalDateTime now, List<FundingStatus> statuses);

    Optional<Funding> findByWishlistItemIdAndStatus(Long wishlistItemId, FundingStatus status);

    @Query("SELECT f FROM Funding f WHERE f.id = :id AND (:status IS NULL OR f.status = :status)")
    Optional<Funding> findByIdAndStatus(@Param("id") Long id, @Param("status") FundingStatus status);

    @Query("SELECT f FROM Funding f WHERE f.receiverId = :receiverId AND (:status IS NULL OR f.status = :status)")
    Page<Funding> findAllByReceiverIdAndStatus(@Param("receiverId") Long receiverId, @Param("status") FundingStatus status, Pageable pageable);

    List<Funding> findAllByWishlistItemIdIn(List<Long> wishlistItemIds);

    Boolean existsByProductIdAndStatusIn(Long productId, List<FundingStatus> statuses);

    Page<Funding> findAllByReceiverIdAndStatusIn(Long friendId, List<FundingStatus> fundingStatuses, Pageable pageable);

    List<Funding> findByProductIdAndStatus(Long productId, FundingStatus status);

    List<Funding> findByStatusAndAchievedAtBefore(FundingStatus status, LocalDateTime achievedAt);

    Optional<Funding> findByIdAndReceiverIdAndStatus(Long id, Long receiverId, FundingStatus status);

    Page<Funding> findAllByReceiverIdInAndStatus(List<Long> receiverIds, FundingStatus status, Pageable pageable);
    Optional<Funding> findActiveByWishlistItemId(Long wishlistItemId);

    Optional<Funding> findByWishlistItemId(Long wishlistItemId);
}
