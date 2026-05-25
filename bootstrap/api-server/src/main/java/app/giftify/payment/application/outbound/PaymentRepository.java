package app.giftify.payment.application.outbound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.support.common.api.paging.Page;
import app.giftify.support.common.api.paging.PageRequest;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findById(Long paymentId);

	Slice<Payment> findPendingPaymentsBefore(LocalDateTime threshold, Pageable pageable);

	Optional<Payment> findByPaymentKey(String paymentKey);

	Optional<Payment> findByOrderNumber(String orderNumber);

	List<Payment> findAllByOrderIdIn(List<Long> orderIds);

	List<Payment> findAllByOrderIdInAndStatusIn(List<Long> orderIds, List<PaymentStatus> statuses);

	Page<Payment> findByMemberId(Long memberId, PageRequest pageRequest);
}
