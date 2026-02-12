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

	/**
	 * orderId로 결제 목록을 조회합니다.
	 * Order BC 연동을 위한 메서드입니다.
	 *
	 * @param orderId Order BC에서 발급한 주문 대체키
	 * @return 해당 orderId로 생성된 결제 목록
	 */
	List<Payment> findByOrderId(String orderId);

	Page<Payment> findByMemberId(Long memberId, PageRequest pageRequest);
}
