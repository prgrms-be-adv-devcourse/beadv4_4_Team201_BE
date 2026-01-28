package app.giftify.out;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.domain.FundingMember;

public interface FundingMemberRepository extends JpaRepository<FundingMember, Long> {
}
