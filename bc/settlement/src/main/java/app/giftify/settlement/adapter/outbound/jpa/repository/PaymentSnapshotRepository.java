package app.giftify.settlement.adapter.outbound.jpa.repository;

import app.giftify.settlement.domain.PaymentSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentSnapshotRepository extends JpaRepository<PaymentSnapshot, Long> {
    Optional<PaymentSnapshot> findByOrderNumber(String orderNumber);
}
