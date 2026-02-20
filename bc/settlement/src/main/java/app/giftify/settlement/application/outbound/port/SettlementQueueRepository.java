package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.model.SettlementQueue;

import java.util.List;

public interface SettlementQueueRepository {

    List<Long> findDistinctSellerIdsByStatusReady();

    List<SettlementQueue> findAllReadyQueuesBySellerId(Long sellerId);

    void saveAll(List<SettlementQueue> queues);
}