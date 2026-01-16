package payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.payment.PaymentFailedEvent;
import app.giftify.shared.domain.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.payment.PaymentType;
import domain.payment.Payment;
import domain.payment.PaymentCreateContext;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import domain.payment.PaymentStatus;
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
			null, // 예치금 충전엔 필요 없음
			type
		);

		policy.validate(chargeContext);

		Payment payment = Payment.builder()
			.userId(command.userId())
			.amount(command.amount())
			.type(type)
			.status(PaymentStatus.PENDING)
			.createdAt(LocalDateTime.now())
			.build();

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
			// 성공 처리: 도메인 상태 변경 (PENDING -> PAID)
			payment.markAsPaid(pgTransactionId);
			paymentRepository.save(payment);

			// 성공 이벤트 발행 -> After Commit으로
			eventPublisher.publish(new PaymentSucceededEvent(
				payment.getPaymentId(),
				payment.getModelType(),
				payment.getUserId(),
				payment.getAmount(),
				payment.getType()
			));

		} else {
			// 실패 처리
			payment.markAsFailed();
			paymentRepository.save(payment);

			// 실패 이벤트 발행
			eventPublisher.publish(new PaymentFailedEvent(
				payment.getPaymentId(),
				payment.getUserId(),
				payment.getAmount(),
				payment.getType(),
				"PG사 승인 거절"
			));
		}
	}
}