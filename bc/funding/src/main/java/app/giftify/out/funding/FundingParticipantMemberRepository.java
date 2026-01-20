package app.giftify.out.funding;

import app.giftify.domain.funding.Funding;
import app.giftify.domain.funding.FundingParticipantMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FundingParticipantMemberRepository extends JpaRepository<FundingParticipantMember, Long> {

    @Query("SELECT SUM(fpm.amount) FROM FundingParticipantMember fpm WHERE fpm.funding.id = :fundingId AND fpm.fundingMemberId = :memberId")
    Optional<Integer> findTotalAmountByFundingIdAndMemberId(@Param("fundingId") Long fundingId, @Param("memberId") Long memberId);

    boolean existsByFundingIdAndFundingMemberId(Long fundingId, Long memberId);

    @Query("SELECT DISTINCT fpm.funding FROM FundingParticipantMember fpm WHERE fpm.fundingMemberId = :memberId")
    Page<Funding> findAllFundingsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // N+1 문제 해결을 위한 DTO 프로젝션 쿼리
    @Query("SELECT new app.giftify.out.funding.MyFundingInfo(fpm.funding, SUM(fpm.amount)) " +
           "FROM FundingParticipantMember fpm " +
           "WHERE fpm.fundingMemberId = :memberId " +
           "GROUP BY fpm.funding")
    Page<MyFundingInfo> findAllMyFundingInfos(@Param("memberId") Long memberId, Pageable pageable);
}
