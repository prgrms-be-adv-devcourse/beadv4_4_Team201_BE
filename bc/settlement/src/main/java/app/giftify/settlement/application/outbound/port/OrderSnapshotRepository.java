package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.snapshot.OrderSnapshot;

public interface OrderSnapshotRepository {
    OrderSnapshot save(OrderSnapshot snapshot);

    OrderSnapshot getById(Long orderId);
}
