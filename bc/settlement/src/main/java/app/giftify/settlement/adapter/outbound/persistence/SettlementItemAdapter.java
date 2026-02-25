package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaSettlementItemRepository;
import app.giftify.settlement.application.outbound.port.SettlementItemRepository;
import app.giftify.settlement.application.service.dto.SettlementSummary;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.model.SettlementItem;
import app.giftify.settlement.domain.model.SettlementItemType;
import app.giftify.settlement.domain.status.SettlementItemStatus;
import app.giftify.shared.api.AmountSummaryProjection;
import app.giftify.shared.api.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettlementItemAdapter implements SettlementItemRepository {

    private final JpaSettlementItemRepository jpaSettlementItemRepository;

    @Override
    public void save(SettlementItem settlementItem) {
        jpaSettlementItemRepository.save(settlementItem);
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

    @Override
    public boolean existsByOrderItemIdAndType(Long orderItemId, SettlementItemType type) {
        return jpaSettlementItemRepository.existsByOrderItemIdAndType(orderItemId, type);
    }

    @Override
    public SettlementItem getByOrderIdAndOrderItemIdAndTypeWithLock(Long orderId, Long orderItemId, SettlementItemType type) {
        return jpaSettlementItemRepository.findByOrderIdAndOrderItemIdAndTypeWithLock(orderId, orderItemId, type)
                .orElseThrow(() -> new DomainException(
                        SettlementErrorCode.SETTLEMENT_ITEM_NOT_FOUND,
                        String.format("정산 아이템이 존재하지 않습니다. OrderID: %d, OrderItemID: %d, Type: %s", orderId, orderItemId, type)
                ));
    }

    @Override
    public Page<SettlementSummary> getSettlementSummary(Long sellerId, Pageable pageable) {
        Page<JpaSettlementItemRepository.SettlementSummaryMapping> rawPage = jpaSettlementItemRepository.findSettlementSummary(sellerId, pageable);

        return rawPage.map(m -> new SettlementSummary(
                m.getSettlementMonth(),
                m.getOrderId(),
                m.getTotalSalesAmount().longValue(),
                m.getTotalSettlementAmount().longValue(),
                m.getItemCount(),
                m.getStatus()
        ));
    }
}
