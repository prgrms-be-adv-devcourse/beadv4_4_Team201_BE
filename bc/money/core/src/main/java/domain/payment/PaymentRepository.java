package domain.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
	Payment save(Payment payment);

	Optional<Payment> findById(Long paymentId);

	List<Payment> findPendingPaymentsBefore(LocalDateTime threshold);

	Optional<Payment> findByPgTransactionId(String pgTransactionId);

	/**
	 * orderUuid로 결제 목록을 조회합니다.
	 * Order BC 연동을 위한 메서드입니다.
	 *
	 * @param orderUuid Order BC에서 발급한 주문 대체키
	 * @return 해당 orderUuid로 생성된 결제 목록
	 */
	List<Payment> findByOrderUuid(String orderUuid);
}
