package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.SettlementItem;
import app.giftify.settlement.domain.SettlementItemStatus;
import app.giftify.shared.api.AmountSummaryProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementItemRepository {

    SettlementItem save(SettlementItem settlementItem);

    List<SettlementItem> getAllByOrderId(Long orderId);

    List<Long> findPendingOrderIds(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit);

    List<AmountSummaryProjection> getSettlementSumByOrderIds(List<Long> orderIds);

    Long getMinOrderId(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit);

    Long getMaxOrderId(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit);
}
