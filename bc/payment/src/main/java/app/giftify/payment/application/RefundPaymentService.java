package app.giftify.payment.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.RefundPaymentCommand;
import app.giftify.payment.application.inbound.RefundPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentRefundedEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class RefundPaymentService implements RefundPaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;

	public RefundPaymentService(
		PaymentRepository paymentRepository,
		EventPublisher eventPublisher
	) {
		this.paymentRepository = paymentRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void refund(RefundPaymentCommand command) {
		// 1. 결제 조회
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[RefundPaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
			));

		// 2. 권한 검증
		if (!payment.isOwnedBy(command.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[RefundPaymentService] 결제 환불 권한이 없습니다. requesterId=" + command.requesterId()
			);
		}

		// 3. 상태 변경
		LocalDateTime refundedAt = LocalDateTime.now();
		String requestId = UUID.randomUUID().toString();
		payment.markAsRefunded(refundedAt, requestId);

		// 4. 저장
		Payment savedPayment = paymentRepository.save(payment);

		// 5. 이벤트 발행
		eventPublisher.publish(new PaymentRefundedEvent(
			savedPayment.getId(),
			savedPayment.getType().name(),
			savedPayment.getMemberId(),
			savedPayment.getPaidAmount(),
			savedPayment.getType(),
			command.reason(),
			refundedAt
		));
	}
}
