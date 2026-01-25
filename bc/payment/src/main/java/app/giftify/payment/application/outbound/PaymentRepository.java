package app.giftify.payment.application.outbound;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import app.giftify.payment.domain.Payment;

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

	/**
	 * 멱등성 키로 결제를 조회합니다.
	 * 결제 생성 요청의 중복 여부를 확인하는 데 사용됩니다.
	 *
	 * @param idempotencyKey 클라이언트/Order BC에서 전달받은 멱등성 키
	 * @return 해당 키로 생성된 결제 (없으면 empty)
	 */
	Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
