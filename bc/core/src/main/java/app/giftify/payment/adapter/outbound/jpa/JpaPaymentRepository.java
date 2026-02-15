package app.giftify.payment.adapter.outbound.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPayment;
import app.giftify.payment.domain.PaymentStatus;

@Repository("paymentBcJpaPaymentRepository")
public interface JpaPaymentRepository extends JpaRepository<JpaPayment, Long> {

	List<JpaPayment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);

	Optional<JpaPayment> findByPaymentKey(String paymentKey);

	Optional<JpaPayment> findByOrderNumber(String orderNumber);

	List<JpaPayment> findAllByOrderIdIn(List<Long> orderIds);

	Page<JpaPayment> findByMemberId(Long memberId, Pageable pageable);
}
