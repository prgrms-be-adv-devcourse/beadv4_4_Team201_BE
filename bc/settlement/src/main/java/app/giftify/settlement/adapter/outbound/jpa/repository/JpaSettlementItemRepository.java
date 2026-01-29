package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.adapter.outbound.jpa.entity.JpaSettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSettlementItemRepository extends JpaRepository<JpaSettlementItem, Long> {
    boolean existsByOrderItemId(Long orderItemId);
}
