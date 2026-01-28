package app.giftify.funding.out;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.funding.domain.FundingMember;

public interface FundingMemberRepository extends JpaRepository<FundingMember, Long> {
}
