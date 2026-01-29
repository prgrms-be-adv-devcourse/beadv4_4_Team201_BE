package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.domain.PaymentSnapshot;
import app.giftify.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentSnapshotRepository extends JpaRepository<PaymentSnapshot, Long> {
}
