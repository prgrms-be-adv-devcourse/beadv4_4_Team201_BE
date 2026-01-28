package app.giftify.payment.adapter.outbound.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;

@Repository("paymentBcJpaPaymentHistoryRepository")
public interface JpaPaymentHistoryRepository extends JpaRepository<JpaPaymentHistory, Long> {

	List<JpaPaymentHistory> findByPaymentIdOrderByOccurredAtAsc(Long paymentId);

	Optional<JpaPaymentHistory> findByIdempotencyKey(String idempotencyKey);
}
