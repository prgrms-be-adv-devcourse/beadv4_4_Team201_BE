package payment.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.event.payment.PaymentType;
import domain.payment.Payment;
import domain.payment.PaymentCreateContext;
import domain.payment.PaymentErrorCode;
import domain.payment.PaymentException;
import domain.payment.PaymentHistory;
import domain.payment.PaymentPolicy;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentCancelUseCase;
import payment.usecase.PaymentChargeUseCase;
import payment.usecase.PaymentCompleteUseCase;
import payment.usecase.command.CancelPaymentCommand;
import payment.usecase.command.PaymentChargeCommand;
import payment.usecase.result.PaymentResult;

@Service
@Transactional
public class PaymentService implements PaymentChargeUseCase, PaymentCompleteUseCase, PaymentCancelUseCase {

	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

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
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.UNSUPPORTED_PAYMENT_TYPE,
				"[Payment] 지원하지 않는 결제 타입입니다: " + type));

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

		// FIXME 충전 완료 이벤트 발행 필요 -> Wallet 등으로

		return new PaymentResult(
			savedPayment.getPaymentId(),
			savedPayment.getStatus(),
			savedPayment.getAmount()
		);
	}

	@Override
	public void complete(Long paymentId, String pgTransactionId, boolean isSuccess) {
		Payment payment = paymentRepository.findById(paymentId)
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[Payment] 결제 내역을 찾을 수 없습니다: " + paymentId));

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
				"PG사 승인 거절",
				history.occurredAt()
			));
		}
	}

	@Override
	public void cancel(CancelPaymentCommand command) {
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[Payment] 결제를 찾을 수 없습니다: "
					+ command.paymentId())); // NOTE :: 로직 안에 메시지가 바로 보이는게 눈에 더 잘 들어오는 것 같아서... 어떻게 생각하는지 팀원들과 논의 필요

		// 취소 불가 상태 (이미 취소됨 등) 면 예외 없이 종료 -> 중복 메시지 루프에 빠지지 않기 위해
		// 네트워크 이슈 등으로 "취소 완료" 처리는 했지만, 메시지 브로커(Kafka)에 "처리 완료(Ack)" 신호를 못 보내는 등의 경우,
		// 예외를 던지면 메시지 브로커가 재시도하여 무한 루프에 빠질수도 있음
		if (!payment.isCancelable()) {
			log.info("[Payment] 이미 취소 가능하지 않은 상태입니다. 요청 무시됨. paymentId={}, status={}",
				payment.getPaymentId(), payment.getStatus());
			return;
		}
		String metadata = buildCancelMetadata(command);
		PaymentHistory history = payment.cancel(metadata);
		paymentRepository.save(payment);

		eventPublisher.publish(new PaymentCanceledEvent(
			payment.getPaymentId(),
			payment.getModelType(),
			payment.getUserId(),
			payment.getAmount(),
			payment.getType(),
			command.reason().name(),
			history.occurredAt()
		));
	}

	// FIXME :: 단순한 문자열을 JSON 으로 바꾸는 것이므로 그냥 이렇게 하는게 간편함. 다만 공통 유틸에 Jackson 을 추가하여 사용하도록 변경이 필요할 듯 함
	private String buildCancelMetadata(CancelPaymentCommand command) {
		if (command.metadata() != null && !command.metadata().isEmpty()) {
			return command.metadata();
		}
		return "{\"reason\":\"" + command.reason().name() + "\"}";
	}
}
