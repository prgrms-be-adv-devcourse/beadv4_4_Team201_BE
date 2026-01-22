package app.giftify.payment.adapter.out.jpa.repository.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPaymentHistory;
import app.giftify.payment.adapter.out.jpa.mapper.PaymentMapper;
import domain.payment.Payment;
import domain.payment.PaymentHistory;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {
	private final JpaPaymentRepository jpaPaymentRepository;
	private final JpaPaymentHistoryRepository jpaPaymentHistoryRepository;
	private final PaymentMapper mapper;

	public PaymentRepositoryAdapter(
		JpaPaymentRepository jpaPaymentRepository,
		JpaPaymentHistoryRepository jpaPaymentHistoryRepository,
		PaymentMapper mapper
	) {
		this.jpaPaymentRepository = jpaPaymentRepository;
		this.jpaPaymentHistoryRepository = jpaPaymentHistoryRepository;
		this.mapper = mapper;
	}

	@Override
	public Payment save(Payment payment) {
		JpaPayment savedEntity;

		if (payment.getPaymentId() == null) {
			// INSERT (새 결제)
			JpaPayment entity = mapper.toEntity(payment);
			savedEntity = jpaPaymentRepository.save(entity);
		} else {
			// UPDATE (기존 결제)
			JpaPayment entity = jpaPaymentRepository.findById(payment.getPaymentId())
				.orElseThrow(() -> new IllegalArgumentException("[Payment] 엔티티를 찾을 수 없습니다."));
			entity.update(payment);
			savedEntity = jpaPaymentRepository.save(entity);
		}

		// History 저장
		saveUncommittedHistory(savedEntity.getId(), payment);

		return mapper.toDomain(savedEntity);
	}

	@Override
	public Optional<Payment> findById(Long paymentId) {
		return jpaPaymentRepository.findById(paymentId)
			.map(mapper::toDomain);
	}

	@Override
	public List<Payment> findPendingPaymentsBefore(LocalDateTime threshold) {
		return jpaPaymentRepository.findByStatusAndCreatedAtBefore(
			PaymentStatus.PENDING, threshold
		).stream().map(mapper::toDomain).toList();
	}

	@Override
	public Optional<Payment> findByPgTransactionId(String pgTransactionId) {
		return jpaPaymentRepository.findByPgTransactionId(pgTransactionId)
			.map(mapper::toDomain);
	}

	@Override
	public List<Payment> findByOrderUuid(String orderUuid) {
		return jpaPaymentRepository.findByOrderUuid(orderUuid)
			.stream()
			.map(mapper::toDomain)
			.toList();
	}

	/**
	 * uncommittedHistory를 DB에 저장하고 클리어
	 */
	private void saveUncommittedHistory(Long paymentId, Payment payment) {
		for (PaymentHistory history : payment.getUncommittedHistory()) {
			JpaPaymentHistory entity = new JpaPaymentHistory(paymentId, history);
			jpaPaymentHistoryRepository.save(entity);
		}
		payment.clearUncommittedHistory();
	}

}
