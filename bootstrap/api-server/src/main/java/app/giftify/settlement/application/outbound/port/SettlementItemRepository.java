package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementItemType;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.settlement.domain.projection.AmountSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementItemRepository {

    void save(SettlementItem settlementItem);

    List<AmountSummaryProjection> getSettlementSumByOrderIds(List<Long> orderIds);

    List<Long> findDistinctOrderIdsBetween(Long minOrderId, Long maxOrderId, int retryLimit);

    Long getMinOrderId(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit);

    Long getMaxOrderId(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit);

    boolean existsByOrderItemIdAndType(Long orderItemId, SettlementItemType type);

    SettlementItem getByOrderIdAndOrderItemIdAndTypeWithLock(Long orderId, Long orderItemId, SettlementItemType type);

    Page<SettlementSummary> getSettlementSummary(Long sellerId, Pageable pageable);
}
