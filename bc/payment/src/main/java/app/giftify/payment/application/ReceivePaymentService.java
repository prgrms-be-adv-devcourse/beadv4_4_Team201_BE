package app.giftify.payment.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.ReceivePaymentCommand;
import app.giftify.payment.application.inbound.ReceivePaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;

/**
 * 수령 확정 UseCase 구현체.
 * 결제 완료된 상품의 수령을 확정하고, 결제 취소를 차단합니다.
 */
@Slf4j
@Service
@Transactional
public class ReceivePaymentService implements ReceivePaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;

	public ReceivePaymentService(
		PaymentRepository paymentRepository,
		EventPublisher eventPublisher
	) {
		this.paymentRepository = paymentRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void receive(ReceivePaymentCommand command) {
		// 1. 결제 조회
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[ReceivePaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
			));

		// 2. 권한 검증
		if (!payment.isOwnedBy(command.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[ReceivePaymentService] 수령 확정 권한이 없습니다. requesterId=" +
					command.requesterId()
			);
		}

		// 3. 상태 변경
		LocalDateTime receivedAt = LocalDateTime.now();
		String requestId = UUID.randomUUID().toString();
		payment.markAsReceived(receivedAt, requestId);

		// 4. 저장
		paymentRepository.save(payment);

		// 5. 이벤트 발행 (필요시 PaymentReceivedEvent 생성)
		// eventPublisher.publish(new PaymentReceivedEvent(...));
	}
}
