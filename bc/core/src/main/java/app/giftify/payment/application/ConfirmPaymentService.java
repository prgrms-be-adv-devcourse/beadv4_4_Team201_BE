package app.giftify.payment.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.ConfirmPaymentCommand;
import app.giftify.payment.application.inbound.ConfirmPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.event.PaymentPaidEvent;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 승인 UseCase 구현체.
 * PG사 결제 완료 후 Payment 상태를 PAID로 변경합니다.
 */
@Slf4j
@Service
@Transactional
public class ConfirmPaymentService implements ConfirmPaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;
	private final PaymentFieldEncryptor encryptor;

	public ConfirmPaymentService(
		PaymentRepository paymentRepository,
		EventPublisher eventPublisher,
		PaymentFieldEncryptor encryptor
	) {
		this.paymentRepository = paymentRepository;
		this.eventPublisher = eventPublisher;
		this.encryptor = encryptor;
	}

	@Override
	public void confirm(ConfirmPaymentCommand command) {
		// 1. 결제 조회
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[ConfirmPaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
			));

		// 2. 민감 정보 암호화
		String encryptedPaymentKey = encryptor.encrypt(command.paymentKey());
		String encryptedApproveCode = command.approveCode() != null
			? encryptor.encrypt(command.approveCode())
			: null;

		// 3. 상태 변경 (도메인 메서드)
		payment.markAsPaid(
			encryptedPaymentKey,
			encryptedApproveCode,
			command.paidAt(),
			command.paymentKey()  // requestId로 원본 paymentKey 사용
		);

		// 4. 저장 (uncommittedHistory 포함)
		Payment savedPayment = paymentRepository.save(payment);

		// 5. 내부 이벤트 발행 (Handler에서 외부 이벤트로 변환)
		eventPublisher.publish(new PaymentPaidEvent(
			savedPayment.getId(),
			savedPayment.getMemberId(),
			savedPayment.getOrderId(),
			savedPayment.getType(),
			savedPayment.getPaidAmount(),
			command.paidAt()
		));
	}
}
