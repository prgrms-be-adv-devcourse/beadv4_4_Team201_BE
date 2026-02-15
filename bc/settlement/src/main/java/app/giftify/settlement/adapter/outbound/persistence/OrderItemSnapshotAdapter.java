package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaOrderItemSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderItemSnapshotRepository;
import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemSnapshotAdapter implements OrderItemSnapshotRepository {

    private final JpaOrderItemSnapshotRepository jpaOrderItemSnapshotRepository;

    @Override
    public OrderItemSnapshot save(OrderItemSnapshot snapshot) {
        return jpaOrderItemSnapshotRepository.save(snapshot);
    }

    @Override
    public OrderItemSnapshot getByTargetId(Long targetId) {
        return jpaOrderItemSnapshotRepository.findByTargetId(targetId)
                .orElseThrow(() -> new PolicyException(SettlementErrorCode.ORDER_ITEM_SNAPSHOT_NOT_FOUND));
    }
}
