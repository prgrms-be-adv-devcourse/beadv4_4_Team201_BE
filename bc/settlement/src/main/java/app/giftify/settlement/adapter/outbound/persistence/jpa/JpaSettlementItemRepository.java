package app.giftify.settlement.adapter.outbound.persistence.jpa;

import app.giftify.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSettlementItemRepository extends JpaRepository<SettlementItem, Long> {
}
