package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.model.SettlementQueue;
import app.giftify.settlement.domain.status.SettlementQueueStatus;

import java.util.List;

public interface SettlementQueueRepository {

    List<Long> findDistinctSellerIdsByStatus(SettlementQueueStatus status);

    List<SettlementQueue> findAllReadyBySellerId(Long sellerId);

    void saveAll(List<SettlementQueue> queues);
}