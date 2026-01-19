package app.giftify.out;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Funding save(Funding funding);

    List<Funding> findByEndAtBeforeAndStatusIn(LocalDateTime now, List<FundingStatus> statuses);
}
