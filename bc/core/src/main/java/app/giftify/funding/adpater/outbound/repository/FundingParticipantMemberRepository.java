package app.giftify.funding.adpater.outbound.repository;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.jpa.FundingParticipantMember;
import app.giftify.funding.application.MyFundingInfo;
import app.giftify.shared.domain.type.FundingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FundingParticipantMemberRepository extends JpaRepository<FundingParticipantMember, Long> {

    @Query("SELECT SUM(fpm.amount) FROM FundingParticipantMember fpm WHERE fpm.funding.id = :fundingId AND fpm.participantId = :memberId")
    Optional<Integer> findTotalAmountByFundingIdAndParticipantId(@Param("fundingId") Long fundingId, @Param("memberId") Long memberId);

    boolean existsByFundingIdAndParticipantId(Long fundingId, Long memberId);

    @Query("SELECT DISTINCT fpm.funding FROM FundingParticipantMember fpm WHERE fpm.participantId = :memberId")
    Page<Funding> findAllFundingsByParticipantId(@Param("memberId") Long memberId, Pageable pageable);

    // N+1 문제 해결을 위한 DTO 프로젝션 쿼리
    @Query("SELECT new app.giftify.funding.application.MyFundingInfo(fpm.funding, SUM(fpm.amount)) " +
           "FROM FundingParticipantMember fpm " +
           "WHERE fpm.participantId = :memberId " +
           "AND (:status IS NULL OR fpm.funding.status = :status) " +
           "GROUP BY fpm.funding")
    Page<MyFundingInfo> findAllMyFundingInfos(@Param("memberId") Long memberId, @Param("status") FundingStatus status, Pageable pageable);

    FundingParticipantMember findByFundingAndParticipantId(Funding funding, Long participantId);

    List<FundingParticipantMember> findByFundingId(Long id);

    void deleteByFundingIdAndParticipantId(Long fundingId, Long participantId);

    @Query("SELECT fpm.participantId FROM FundingParticipantMember fpm WHERE fpm.funding.id = :fundingId")
    List<Long> findIdsByFundingId(@Param("fundingId") Long fundingId);
}
