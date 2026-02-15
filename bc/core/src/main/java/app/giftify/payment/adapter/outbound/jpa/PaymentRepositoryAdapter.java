package app.giftify.payment.adapter.outbound.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.adapter.outbound.jpa.entity.JpaPayment;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.api.paging.Page;

@Repository("paymentBcPaymentRepositoryAdapter")
public class PaymentRepositoryAdapter implements PaymentRepository {

	private final JpaPaymentRepository jpaPaymentRepository;
	private final PaymentMapper paymentMapper;

	public PaymentRepositoryAdapter(
		JpaPaymentRepository jpaPaymentRepository,
		PaymentMapper paymentMapper
	) {
		this.jpaPaymentRepository = jpaPaymentRepository;
		this.paymentMapper = paymentMapper;
	}

	@Override
	@Transactional
	public Payment save(Payment payment) {
		JpaPayment jpaPayment;

		if (payment.getId() == null) {
			jpaPayment = paymentMapper.toEntity(payment);
			jpaPayment = jpaPaymentRepository.save(jpaPayment);
		} else {
			jpaPayment = jpaPaymentRepository.findById(payment.getId())
				.orElseThrow(() -> new IllegalArgumentException(
					"[PaymentRepositoryAdapter] 존재하지 않는 Payment: " + payment.getId()));
			JpaPayment updatedEntity = paymentMapper.toEntity(payment);
			jpaPayment.updateFrom(payment, updatedEntity.getOrderItemsJson());
		}

		return paymentMapper.toDomain(jpaPayment);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> findById(Long paymentId) {
		return jpaPaymentRepository.findById(paymentId)
			.map(paymentMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Payment> findPendingPaymentsBefore(LocalDateTime threshold) {
		return jpaPaymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.PENDING, threshold)
			.stream()
			.map(paymentMapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> findByPaymentKey(String paymentKey) {
		return jpaPaymentRepository.findByPaymentKey(paymentKey)
			.map(paymentMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Payment> findByOrderNumber(String orderNumber) {
		return jpaPaymentRepository.findByOrderNumber(orderNumber)
			.map(paymentMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Payment> findAllByOrderIdIn(List<Long> orderIds) {
		return jpaPaymentRepository.findAllByOrderIdIn(orderIds)
			.stream()
			.map(paymentMapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<Payment> findByMemberId(Long memberId, app.giftify.shared.api.paging.PageRequest pageRequest) {
		PageRequest springPageRequest = PageRequest.of(pageRequest.page(), pageRequest.size());
		org.springframework.data.domain.Page<JpaPayment> springPage =
			jpaPaymentRepository.findByMemberId(memberId, springPageRequest);

		List<Payment> payments = springPage.getContent()
			.stream()
			.map(paymentMapper::toDomain)
			.toList();

		return Page.of(payments, springPage.getTotalElements());
	}
}
