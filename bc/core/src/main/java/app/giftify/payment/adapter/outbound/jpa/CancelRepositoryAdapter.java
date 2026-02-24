package app.giftify.payment.adapter.outbound.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.outbound.CancelRepository;
import app.giftify.payment.domain.Cancel;

@Repository
public class CancelRepositoryAdapter implements CancelRepository {

	private final JpaCancelRepository jpaCancelRepository;

	public CancelRepositoryAdapter(JpaCancelRepository jpaCancelRepository) {
		this.jpaCancelRepository = jpaCancelRepository;
	}

	@Override
	@Transactional
	public Cancel save(Cancel cancel) {
		var jpaCancel = app.giftify.payment.adapter.outbound.jpa.entity.JpaCancel.from(cancel);
		jpaCancel = jpaCancelRepository.save(jpaCancel);
		return jpaCancel.toDomain();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Cancel> findAllByPaymentId(Long paymentId) {
		return jpaCancelRepository.findAllByPaymentId(paymentId)
			.stream()
			.map(app.giftify.payment.adapter.outbound.jpa.entity.JpaCancel::toDomain)
			.toList();
	}
}
