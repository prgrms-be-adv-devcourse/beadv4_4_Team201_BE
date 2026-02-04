package app.giftify.funding.adpater.outbound.repository;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.shared.domain.type.FundingStatus;
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

    Optional<Funding> findByWishlistItemId(Long wishlistItemId);

    Optional<Funding> findByWishlistItemIdAndStatus(Long wishlistItemId, FundingStatus status);

//    List<Funding> findUnacceptedAchievedFundingsBefore(LocalDateTime deadline);
}
