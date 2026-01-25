package app.giftify.payment.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 취소 UseCase 구현체.
 */
@Slf4j
@Service
@Transactional
public class CancelPaymentService implements CancelPaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;

	public CancelPaymentService(
		PaymentRepository paymentRepository,
		EventPublisher eventPublisher
	) {
		this.paymentRepository = paymentRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void cancel(CancelPaymentCommand command) {
		// 1. 결제 조회
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[CancelPaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
			));

		// 2. 권한 검증
		if (!payment.isOwnedBy(command.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[CancelPaymentService] 결제 취소 권한이 없습니다. requesterId=" + command.requesterId()
			);
		}

		// 3. 상태 변경
		LocalDateTime canceledAt = LocalDateTime.now();
		String requestId = UUID.randomUUID().toString();
		payment.markAsCanceled(canceledAt, requestId);

		// 4. 저장
		Payment savedPayment = paymentRepository.save(payment);

		// 5. 내부 이벤트 발행 (Handler에서 외부 이벤트로 변환)
		eventPublisher.publish(new PaymentCanceledEvent(
			savedPayment.getId(),
			savedPayment.getMemberId(),
			savedPayment.getOrderId(),
			savedPayment.getType(),
			savedPayment.getPaidAmount(),
			command.reason(),
			canceledAt
		));
	}
}
