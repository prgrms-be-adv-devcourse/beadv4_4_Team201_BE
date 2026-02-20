package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.model.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {
}