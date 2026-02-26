package app.giftify.payment.adapter.outbound.jpa;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;
import app.giftify.payment.application.outbound.PaymentHistoryRepository;
import app.giftify.payment.domain.PaymentHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentHistoryRepositoryAdapter implements PaymentHistoryRepository {

	private final JpaPaymentHistoryRepository jpaRepository;

	@Override
	public PaymentHistory save(PaymentHistory history) {
		JpaPaymentHistory entity = JpaPaymentHistory.from(history, history.getPaymentId());
		JpaPaymentHistory saved = jpaRepository.save(entity);
		return saved.toDomain();
	}

	@Override
	public List<PaymentHistory> findByPaymentId(Long paymentId) {
		return jpaRepository.findByPaymentIdOrderByOccurredAtAsc(paymentId).stream()
			.map(JpaPaymentHistory::toDomain)
			.toList();
	}

	@Override
	public Optional<PaymentHistory> findByHistoryKey(String historyKey) {
		return jpaRepository.findByHistoryKey(historyKey)
			.map(JpaPaymentHistory::toDomain);
	}
}
