package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaOrderSnapshotRepository;
import app.giftify.settlement.application.outbound.port.OrderSnapshotRepository;
import app.giftify.settlement.domain.OrderSnapshot;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderSnapshotAdapter implements OrderSnapshotRepository {

    private final JpaOrderSnapshotRepository orderSnapshotRepository;

    @Override
    public OrderSnapshot save(OrderSnapshot snapshot) {
        return orderSnapshotRepository.save(snapshot);
    }

    @Override
    public OrderSnapshot getById(Long orderId) {
        return orderSnapshotRepository.findById(orderId)
                .orElseThrow(() -> new PolicyException(SettlementErrorCode.ORDER_SNAPSHOT_NOT_FOUND));
    }
}
