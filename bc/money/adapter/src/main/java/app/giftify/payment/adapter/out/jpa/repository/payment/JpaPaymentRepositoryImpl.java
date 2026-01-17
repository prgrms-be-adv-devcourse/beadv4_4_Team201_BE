package app.giftify.payment.adapter.out.jpa.repository.payment;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPaymentHistory;
import app.giftify.payment.adapter.out.jpa.mapper.PaymentMapper;
import domain.payment.Payment;
import domain.payment.PaymentHistory;
import domain.payment.PaymentRepository;

@Repository
public class JpaPaymentRepositoryImpl implements PaymentRepository {
	private final JpaPaymentRepository jpaPaymentRepository;
	private final JpaPaymentHistoryRepository jpaPaymentHistoryRepository;  // 추가
	private final PaymentMapper mapper;

	public JpaPaymentRepositoryImpl(
		JpaPaymentRepository jpaPaymentRepository,
		JpaPaymentHistoryRepository jpaPaymentHistoryRepository,  // 추가
		PaymentMapper mapper
	) {
		this.jpaPaymentRepository = jpaPaymentRepository;
		this.jpaPaymentHistoryRepository = jpaPaymentHistoryRepository;  // 추가
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
