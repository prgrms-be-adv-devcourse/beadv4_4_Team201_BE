package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.domain.OrderSnapshot;
import app.giftify.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSnapshotRepository extends JpaRepository<OrderSnapshot, Long> {
}
