package app.giftify.out;

import app.giftify.domain.funding.Funding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Funding save(Funding funding);

}
