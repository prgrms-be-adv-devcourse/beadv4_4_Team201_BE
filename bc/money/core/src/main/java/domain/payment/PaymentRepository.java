package domain.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findById(Long paymentId);

	List<Payment> findPendingPaymentsBefore(LocalDateTime threshold);

	Optional<Payment> findByPgTransactionId(String pgTransactionId);
}
