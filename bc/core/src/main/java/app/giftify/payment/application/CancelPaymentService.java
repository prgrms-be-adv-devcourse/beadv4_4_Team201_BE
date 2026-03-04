package app.giftify.payment.application;

import static app.giftify.payment.domain.SystemConstants.*;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.adapter.outbound.pg.TossCancelResult;
import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentGateway;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.type.CancelType;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 취소 UseCase 구현체.
 */
@Slf4j
@Service
@Transactional
public class CancelPaymentService implements CancelPaymentUseCase {
	private final PaymentRepository paymentRepository;
	private final PaymentGateway paymentGateway;
	private final EventPublisher eventPublisher;
	private final PaymentFieldEncryptor encryptor;

	public CancelPaymentService(
		PaymentRepository paymentRepository, PaymentGateway paymentGateway,
		EventPublisher eventPublisher, PaymentFieldEncryptor encryptor
	) {
		this.paymentRepository = paymentRepository;
		this.paymentGateway = paymentGateway;
		this.eventPublisher = eventPublisher;
		this.encryptor = encryptor;
	}

	@Override
	public void cancel(CancelPaymentCommand command) {
		// 1. 결제 조회
		Payment payment = paymentRepository.findById(command.paymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[CancelPaymentService] 결제를 찾을 수 없습니다. paymentId=" + command.paymentId()
			));

		// 2. 권한 검증 (시스템 권한자는 검증 스킵)
		if (!Objects.equals(command.requesterId(), SYSTEM_REQUESTER_ID)
			&& !payment.isOwnedBy(command.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[CancelPaymentService] 결제 취소 권한이 없습니다. requesterId=" +
					command.requesterId()
			);
		}

		// 3. 상태에 따른 취소 처리
		if (payment.getStatus() == PaymentStatus.PAID) {
			String decryptedPaymentKey = encryptor.decrypt(payment.getPaymentKey());

			TossCancelResult pgResult = paymentGateway.cancel(
				decryptedPaymentKey, command.reason(), null
			);

			if (!pgResult.success()) {
				payment.recordCancelFailed(pgResult.errorMessage(), LocalDateTime.now());
				var failEvents = payment.pullEvents();
				paymentRepository.save(payment);
				failEvents.forEach(eventPublisher::publish);
				return;
			}

			payment.markAsCanceled(CancelType.REFUND, command.reason());
		} else {
			payment.markAsCanceled(CancelType.CANCEL, command.reason());
		}

		// 4. 성공 시 저장 + 이벤트 발행
		var domainEvents = payment.pullEvents();
		paymentRepository.save(payment);
		domainEvents.forEach(eventPublisher::publish);
	}

}
