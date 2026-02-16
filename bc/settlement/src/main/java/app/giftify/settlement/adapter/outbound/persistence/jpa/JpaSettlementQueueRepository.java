package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.settlement.domain.status.SettlementQueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaSettlementQueueRepository extends JpaRepository<SettlementQueue, Long> {

    @Query("""
        SELECT DISTINCT q.item.sellerId
        FROM SettlementQueue q
        WHERE q.status = :status
    """)
    List<Long> findDistinctSellerIdsByStatus(@Param("status") SettlementQueueStatus status);

    @Query("""
        SELECT q FROM SettlementQueue q
        JOIN FETCH q.item
        WHERE q.item.sellerId = :sellerId
          AND q.status = :status
    """)
    List<SettlementQueue> findAllBySellerIdAndStatus(
            @Param("sellerId") Long sellerId,
            @Param("status") SettlementQueueStatus status
    );
}