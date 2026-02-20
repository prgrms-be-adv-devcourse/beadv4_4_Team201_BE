package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.snapshot.OrderItemSnapshot;

public interface OrderItemSnapshotRepository {
    OrderItemSnapshot save(OrderItemSnapshot snapshot);

    OrderItemSnapshot getByTargetId(Long targetId);
}
