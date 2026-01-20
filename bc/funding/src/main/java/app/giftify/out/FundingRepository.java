package app.giftify.out;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Funding save(Funding funding);

    Page<Funding> findAllByStatusIn(List<FundingStatus> statuses, Pageable pageable);
    List<Funding> findByDeadlineAfterAndStatusIn(LocalDateTime now, List<FundingStatus> statuses);
}
