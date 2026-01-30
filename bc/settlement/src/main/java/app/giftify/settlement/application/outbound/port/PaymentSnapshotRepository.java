package app.giftify.settlement.application.outbound.port;

import app.giftify.settlement.domain.PaymentSnapshot;

public interface PaymentSnapshotRepository {
    PaymentSnapshot save(PaymentSnapshot snapshot);

    PaymentSnapshot getByOrderNumber(String orderNumber);
}
