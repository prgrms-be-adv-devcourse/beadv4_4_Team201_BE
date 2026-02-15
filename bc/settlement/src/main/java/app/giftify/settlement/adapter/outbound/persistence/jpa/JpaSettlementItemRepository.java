package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.AmountSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaSettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    List<SettlementItem> findAllByOrderId(Long orderId);

    @Query("""
        SELECT DISTINCT s.orderId
        FROM SettlementItem s
        WHERE s.lifeCycleMeta.status = :status
          AND s.createdAt < :cutOffDateTime
          AND s.retryCount < :retryLimit
    """)
    List<Long> findPendingOrderIds(
            @Param("status") SettlementItemStatus status,
            @Param("cutOffDateTime") LocalDateTime cutOffDateTime,
            @Param("retryLimit") int retryLimit
    );

    @Query(
            value = """
            SELECT s.order_id as orderId,
                   SUM(s.settlement_amount) as total
            FROM settlement_item s
            WHERE s.order_id IN :orderIds
            GROUP BY s.order_id
        """,
            nativeQuery = true
    )
    List<AmountSummaryProjection> findSettlementSumByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Query("""
        SELECT MIN(s.orderId)
        FROM SettlementItem s
        WHERE s.lifeCycleMeta.status = :status
          AND s.createdAt < :cutOffDateTime
          AND s.retryCount < :retryLimit
    """)
    Long findMinOrderId(
            @Param("status") SettlementItemStatus status,
            @Param("cutOffDateTime") LocalDateTime cutOffDateTime,
            @Param("retryLimit") int retryLimit
    );

    @Query("""
        SELECT MAX(s.orderId)
        FROM SettlementItem s
        WHERE s.lifeCycleMeta.status = :status
          AND s.createdAt < :cutOffDateTime
          AND s.retryCount < :retryLimit
    """)
    Long findMaxOrderId(
            @Param("status") SettlementItemStatus status,
            @Param("cutOffDateTime") LocalDateTime cutOffDateTime,
            @Param("retryLimit") int retryLimit
    );
}
