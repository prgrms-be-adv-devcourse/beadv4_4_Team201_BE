package app.giftify.out;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.funding.Funding;

public interface FundingRepository extends JpaRepository<Funding, Long> {

	Funding save(Funding funding);

}
