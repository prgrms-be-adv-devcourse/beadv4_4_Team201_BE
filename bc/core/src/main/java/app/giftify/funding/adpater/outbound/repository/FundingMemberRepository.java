package app.giftify.funding.adpater.outbound.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.funding.adpater.outbound.jpa.FundingMember;

public interface FundingMemberRepository extends JpaRepository<FundingMember, Long> {
}
