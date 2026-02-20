package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaSettlementHistoryRepository;
import app.giftify.settlement.application.outbound.port.SettlementHistoryRepository;
import app.giftify.settlement.domain.model.SettlementHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SettlementHistoryAdapter implements SettlementHistoryRepository {

    private final JpaSettlementHistoryRepository jpaSettlementHistoryRepository;

    @Override
    public SettlementHistory save(SettlementHistory history) {
        return jpaSettlementHistoryRepository.save(history);
    }
}