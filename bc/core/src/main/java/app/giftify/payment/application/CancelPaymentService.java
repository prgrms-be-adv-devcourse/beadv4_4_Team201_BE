package app.giftify.payment.application;

import static app.giftify.payment.domain.SystemConstants.SYSTEM_REQUESTER_ID;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.CancelPaymentCommand;
import app.giftify.payment.application.inbound.CancelPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
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

		// 2. 권한 검증 (시스템 호출자는 스킵)
		if (!Objects.equals(command.requesterId(), SYSTEM_REQUESTER_ID)
			&& !payment.isOwnedBy(command.requesterId())) {
			throw new PaymentException(
				PaymentErrorCode.UNAUTHORIZED_ACCESS,
				"[CancelPaymentService] 결제 취소 권한이 없습니다. requesterId=" + command.requesterId()
			);
		}

		// 3. 상태 변경 (도메인 메서드 — 내부적으로 registerEvent 호출)
		payment.markAsCanceled(CancelType.CANCEL, command.reason());

		// 4. 도메인 이벤트 확보 → 저장 → 발행
		var domainEvents = payment.pullEvents();
		paymentRepository.save(payment);
		domainEvents.forEach(eventPublisher::publish);
	}
}
