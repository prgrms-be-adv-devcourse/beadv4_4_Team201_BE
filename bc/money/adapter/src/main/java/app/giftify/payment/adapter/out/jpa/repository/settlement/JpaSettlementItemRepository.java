package app.giftify.payment.adapter.out.jpa.repository.settlement;

import app.giftify.payment.adapter.out.jpa.entity.settlement.JpaSettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSettlementItemRepository extends JpaRepository<JpaSettlementItem, Long> {
    boolean existsByOrderItemId(Long orderItemId);
}
