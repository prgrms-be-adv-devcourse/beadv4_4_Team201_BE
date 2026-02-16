package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaSettlementItemRepository;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.AmountSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettlementItemAdapter implements SettlementItemRepository {

    private final JpaSettlementItemRepository jpaSettlementItemRepository;

    @Override
    public SettlementItem save(SettlementItem settlementItem) {
        return jpaSettlementItemRepository.save(settlementItem);
    }

    @Override
    public List<SettlementItem> getAllByOrderId(Long orderId) {
        return jpaSettlementItemRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<Long> findPendingOrderIds(
            SettlementItemStatus status,
            LocalDateTime cutOffDateTime,
            int retryLimit
    ) {
        return jpaSettlementItemRepository.findPendingOrderIds(status, cutOffDateTime, retryLimit);
    }

    @Override
    public List<AmountSummaryProjection> getSettlementSumByOrderIds(List<Long> orderIds) {
        return jpaSettlementItemRepository.findSettlementSumByOrderIds(orderIds);
    }

    @Override
    public List<Long> findDistinctOrderIdsBetween(Long minOrderId, Long maxOrderId, int retryLimit) {
        return jpaSettlementItemRepository.findDistinctOrderIdsBetween(minOrderId, maxOrderId, retryLimit);
    }

    @Override
    public Long getMinOrderId(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit) {
        return jpaSettlementItemRepository.findMinOrderId(status, cutOffDateTime, retryLimit);
    }

    @Override
    public Long getMaxOrderId(SettlementItemStatus status, LocalDateTime cutOffDateTime, int retryLimit) {
        return jpaSettlementItemRepository.findMaxOrderId(status, cutOffDateTime, retryLimit);
    }
}
