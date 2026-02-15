package app.giftify.settlement.adapter.outbound.persistence;

import app.giftify.settlement.adapter.outbound.persistence.jpa.JpaPaymentSnapshotRepository;
import app.giftify.settlement.application.outbound.port.PaymentSnapshotRepository;
import app.giftify.settlement.domain.snapshot.PaymentSnapshot;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentSnapshotAdapter implements PaymentSnapshotRepository {

    private final JpaPaymentSnapshotRepository paymentSnapshotRepository;

    @Override
    public PaymentSnapshot save(PaymentSnapshot snapshot) {
        return paymentSnapshotRepository.save(snapshot);
    }

    @Override
    public PaymentSnapshot getByOrderNumber(String orderNumber) {
        return paymentSnapshotRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PolicyException(SettlementErrorCode.PAYMENT_SNAPSHOT_NOT_FOUND));
    }
}
