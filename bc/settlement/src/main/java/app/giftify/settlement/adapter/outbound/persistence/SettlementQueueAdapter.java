package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaSettlementQueueRepository;
import app.giftify.settlement.application.outbound.port.SettlementQueueRepository;
import app.giftify.settlement.domain.model.SettlementQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettlementQueueAdapter implements SettlementQueueRepository {

    private final JpaSettlementQueueRepository jpaSettlementQueueRepository;

    @Override
    public List<Long> findDistinctSellerIdsByStatusReady() {
        return jpaSettlementQueueRepository.findDistinctSellerIdsByStatusReady();
    }

    @Override
    public List<SettlementQueue> findAllReadyQueuesBySellerId(Long sellerId) {
        return jpaSettlementQueueRepository.findAllReadyQueuesBySellerId(sellerId);
    }

    @Override
    public void saveAll(List<SettlementQueue> queues) {
        jpaSettlementQueueRepository.saveAll(queues);
    }
}