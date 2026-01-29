package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.domain.OrderItemSnapshot;
import app.giftify.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemSnapshotRepository extends JpaRepository<OrderItemSnapshot, Long> {
}
