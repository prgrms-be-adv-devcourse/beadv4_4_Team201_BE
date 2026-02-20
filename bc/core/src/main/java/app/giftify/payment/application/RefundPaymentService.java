package app.giftify.payment.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.RefundPaymentCommand;
import app.giftify.payment.application.inbound.RefundPaymentUseCase;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentRefundedExternalEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 환불 UseCase 구현체.
 */
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

		// 3. 상태 변경 (도메인 메서드 — 내부적으로 registerEvent 호출)
		LocalDateTime refundedAt = LocalDateTime.now();
		payment.markAsRefunded(command.refundAmount(), refundedAt, command.reason());

		// 4. 도메인 이벤트 확보 → 저장 → 발행
		var domainEvents = payment.pullEvents();
		Payment savedPayment = paymentRepository.save(payment);
		domainEvents.forEach(eventPublisher::publish);

		// 6. 외부 BC용 이벤트 직접 발행 (Settlement BC)
		List<Long> sellerIds = savedPayment.getOrderItems().stream()
			.map(OrderItemSnapshot::sellerId)
			.distinct()
			.toList();

		eventPublisher.publish(PaymentRefundedExternalEvent.create(
			savedPayment.getId(),
			command.refundAmount(),
			sellerIds,
			refundedAt
		));
	}
}
