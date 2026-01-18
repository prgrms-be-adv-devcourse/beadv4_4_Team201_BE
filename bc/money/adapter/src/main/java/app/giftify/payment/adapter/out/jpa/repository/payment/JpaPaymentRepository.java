package app.giftify.payment.adapter.out.jpa.repository.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import domain.payment.PaymentStatus;

public interface JpaPaymentRepository extends JpaRepository<JpaPayment, Long> {
	/**
	 * @param status 결제 상태
	 * @param threshold 검색 범위
	 * @return PENDING 이면서 검색 범위에 있는 JpaPayment 들을 반환한다
	 */
	List<JpaPayment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);

	/**
	 * @param pgTransactionId pgTransactionID 로 결제를 조회한다
	 */
	Optional<JpaPayment> findByPgTransactionId(String pgTransactionId);
}
