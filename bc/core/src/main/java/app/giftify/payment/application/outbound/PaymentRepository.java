package app.giftify.payment.application.outbound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import app.giftify.payment.domain.Payment;
import app.giftify.shared.api.paging.Page;
import app.giftify.shared.api.paging.PageRequest;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findById(Long paymentId);

	List<Payment> findPendingPaymentsBefore(LocalDateTime threshold);

	Optional<Payment> findByPaymentKey(String paymentKey);

	Optional<Payment> findByOrderId(String orderId);

	Page<Payment> findByMemberId(Long memberId, PageRequest pageRequest);
}
