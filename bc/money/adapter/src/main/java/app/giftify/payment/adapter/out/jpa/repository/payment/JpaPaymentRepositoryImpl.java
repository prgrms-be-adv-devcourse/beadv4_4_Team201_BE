package app.giftify.payment.adapter.out.jpa.repository.payment;

import app.giftify.payment.adapter.out.jpa.entity.payment.JpaPayment;
import app.giftify.payment.adapter.out.jpa.mapper.PaymentMapper;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaPaymentRepositoryImpl implements PaymentRepository {
	private final JpaPaymentRepository jpaPaymentRepository;
	private final PaymentMapper mapper;

	public JpaPaymentRepositoryImpl(JpaPaymentRepository jpaPaymentRepository, PaymentMapper mapper) {
		this.jpaPaymentRepository = jpaPaymentRepository;
		this.mapper = mapper;
	}

	@Override
	public Payment save(Payment payment) {
		if (payment.getPaymentId() == null) {
			JpaPayment entity = mapper.toEntity(payment);
			return mapper.toDomain(jpaPaymentRepository.save(entity));
		} else {
			JpaPayment entity = jpaPaymentRepository.findById(payment.getPaymentId())
				.orElseThrow(() -> new IllegalArgumentException("Entity not found"));
			entity.update(payment);
			return mapper.toDomain(jpaPaymentRepository.save(entity));
		}
	}

	@Override
	public Optional<Payment> findById(Long paymentId) {
		return jpaPaymentRepository.findById(paymentId)
			.map(mapper::toDomain);
	}
}
