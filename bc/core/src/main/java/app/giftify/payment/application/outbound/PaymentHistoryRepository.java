package app.giftify.payment.application.outbound;

import app.giftify.payment.domain.PaymentHistory;

import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository {
	PaymentHistory save(PaymentHistory history);
	List<PaymentHistory> findByPaymentId(Long paymentId);
	Optional<PaymentHistory> findByHistoryKey(String historyKey);
}
