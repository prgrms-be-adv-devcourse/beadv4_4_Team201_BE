package payment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.event.payment.PaymentType;
import domain.payment.Payment;
import domain.payment.PaymentCreateContext;
import domain.payment.PaymentHistory;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentChargeUseCase;
import payment.usecase.PaymentCompleteUseCase;
import payment.usecase.command.PaymentChargeCommand;
import payment.usecase.result.PaymentResult;

@Service
@Transactional
public class PaymentService implements PaymentChargeUseCase, PaymentCompleteUseCase {
	private final List<PaymentPolicy> policies;
	private final EventPublisher eventPublisher;
	private final PaymentRepository paymentRepository;

	public PaymentService(PaymentRepository paymentRepository, List<PaymentPolicy> policies,
		EventPublisher eventPublisher) {
		this.paymentRepository = paymentRepository;
		this.policies = policies;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public PaymentResult charge(PaymentChargeCommand command) {
		PaymentType type = PaymentType.CHARGE;

		PaymentPolicy policy = policies.stream().filter(p -> p.support(type))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("[Payment] 지원하지 않는 결제 타입입니다"));

		PaymentCreateContext chargeContext = new PaymentCreateContext(
			command.userId(),
			command.amount(),
			type
		);

		policy.validate(chargeContext);

		Payment payment = Payment.create(
			command.userId(),
			type,
			command.amount(),
			null  // PaymentMethod - 필요시 command에서 받기
		);

		var savedPayment = paymentRepository.save(payment);

		return new PaymentResult(
			savedPayment.getPaymentId(),
			savedPayment.getStatus(),
			savedPayment.getAmount()
		);
	}

	@Override
	public void complete(Long paymentId, String pgTransactionId, boolean isSuccess) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new IllegalArgumentException("[Payment] 결제 내역을 찾을 수 없습니다: " + paymentId));

		if (isSuccess) {
			PaymentHistory history = payment.markAsPaid(pgTransactionId);
			paymentRepository.save(payment);

			eventPublisher.publish(new PaymentSucceededEvent(
				payment.getPaymentId(),
				payment.getModelType(),
				payment.getUserId(),
				payment.getAmount(),
				payment.getType(),
				history.occurredAt()
			));

		} else {
			PaymentHistory history = payment.markAsFailed();
			paymentRepository.save(payment);

			eventPublisher.publish(new PaymentFailedEvent(
				payment.getPaymentId(),
				payment.getModelType(),
				payment.getUserId(),
				payment.getAmount(),
				payment.getType(),
				"PG사 승인 거절"
			));
		}
	}
}
