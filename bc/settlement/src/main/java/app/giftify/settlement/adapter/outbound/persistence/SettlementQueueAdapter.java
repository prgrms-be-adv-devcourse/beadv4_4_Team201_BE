package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaSettlementQueueRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.settlement.domain.status.SettlementQueueStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettlementQueueAdapter implements SettlementQueueRepository {

    private final JpaSettlementQueueRepository jpaSettlementQueueRepository;

    @Override
    public List<Long> findDistinctSellerIdsByStatus(SettlementQueueStatus status) {
        return jpaSettlementQueueRepository.findDistinctSellerIdsByStatus(status);
    }

    @Override
    public List<SettlementQueue> findAllBySellerIdAndStatus(Long sellerId, SettlementQueueStatus status) {
        return jpaSettlementQueueRepository.findAllBySellerIdAndStatus(sellerId, status);
    }
}