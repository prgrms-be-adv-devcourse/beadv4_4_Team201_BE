package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementItemType;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.AmountSummaryProjection;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaSettlementItemRepository extends JpaRepository<SettlementItem, Long> {
    List<SettlementItem> findAllByOrderId(Long orderId);

    @Query("""
        SELECT DISTINCT s.orderId
        FROM SettlementItem s
        WHERE s.statusInfo.status = :status
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
        SELECT DISTINCT s.orderId
        FROM SettlementItem s
        WHERE s.orderId BETWEEN :minOrderId AND :maxOrderId
          AND s.retryCount < :retryLimit
    """)
    List<Long> findDistinctOrderIdsBetween(
            @Param("minOrderId") Long minOrderId,
            @Param("maxOrderId") Long maxOrderId,
            @Param("retryLimit") int retryLimit
    );

    @Query("""
        SELECT MIN(s.orderId)
        FROM SettlementItem s
        WHERE s.statusInfo.status = :status
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
        WHERE s.statusInfo.status = :status
          AND s.createdAt < :cutOffDateTime
          AND s.retryCount < :retryLimit
    """)
    Long findMaxOrderId(
            @Param("status") SettlementItemStatus status,
            @Param("cutOffDateTime") LocalDateTime cutOffDateTime,
            @Param("retryLimit") int retryLimit
    );

    boolean existsByOrderItemIdAndType(Long orderItemId, SettlementItemType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM SettlementItem s
        WHERE s.orderId = :orderId
            AND s.orderItemId = :orderItemId
            AND s.type = :type
    """)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    Optional<SettlementItem> findByOrderIdAndOrderItemIdAndTypeWithLock(Long orderId, Long orderItemId, SettlementItemType type);

    @Query(value = """
        SELECT 
            TO_CHAR(COALESCE(settled_at, expected_date), 'YYYY-MM') as settlementMonth,
            order_id as orderId,
            SUM(paid_amount) as totalSalesAmount,
            SUM(settlement_amount) as totalSettlementAmount,
            COUNT(id) as itemCount,
            CASE
                WHEN BOOL_OR(status = 'FAILED') THEN 'FAILED'
                WHEN BOOL_OR(status = 'VALIDATE_FAILED') THEN 'VALIDATE_FAILED'
                WHEN BOOL_OR(status = 'MANUAL') THEN 'MANUAL'
                WHEN BOOL_OR(status = 'PROCESSING') THEN 'PROCESSING'
                WHEN BOOL_OR(status = 'VALIDATING') THEN 'VALIDATING'
                WHEN BOOL_OR(status = 'READY') THEN 'READY'
                WHEN BOOL_OR(status = 'CREATED') THEN 'CREATED'
                WHEN BOOL_OR(status = 'CANCELLED') AND NOT BOOL_OR(status != 'CANCELLED') THEN 'CANCELLED'
                ELSE 'COMPLETED'
            END as status
        FROM settlement_item
        WHERE seller_id = :sellerId
        GROUP BY TO_CHAR(COALESCE(settled_at, expected_date), 'YYYY-MM'), order_id
        ORDER BY settlementMonth DESC, orderId DESC
    """, countQuery = """
        SELECT COUNT(*) FROM (
            SELECT DISTINCT TO_CHAR(COALESCE(settled_at, expected_date), 'YYYY-MM'), order_id
            FROM settlement_item
            WHERE seller_id = :sellerId
        ) AS temp
    """, nativeQuery = true)
    Page<SettlementSummaryMapping> findSettlementSummary(@Param("sellerId") Long sellerId, Pageable pageable);

    // 요약 결과 매핑 인터페이스
    interface SettlementSummaryMapping {
        String getSettlementMonth();
        Long getOrderId();
        BigDecimal getTotalSalesAmount();
        BigDecimal getTotalSettlementAmount();
        Long getItemCount(); // 아이템 총 개수
        String getStatus(); // MAX로 뽑힌 대표 상태
    }
}
