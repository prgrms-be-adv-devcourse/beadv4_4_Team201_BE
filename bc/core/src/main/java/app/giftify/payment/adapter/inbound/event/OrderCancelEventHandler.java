package app.giftify.payment.adapter.inbound.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.outbound.CancelRepository;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Cancel;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.order.OrderCancelRequestedEvent;
import app.giftify.shared.domain.type.CancelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelEventHandler {

	private final PaymentRepository paymentRepository;
	private final CancelRepository cancelRepository;
	private final EventPublisher eventPublisher;

	@ApplicationModuleListener
	@Transactional
	public void handle(OrderCancelRequestedEvent event) {
		log.info("[OrderCancelEventHandler] 주문 취소 요청 이벤트 수신. orderId={}, paymentId={}, cancelAmount={}",
			event.getOrderId(), event.getPaymentId(), event.getCancelAmount());

		Payment payment = paymentRepository.findById(event.getPaymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[OrderCancelEventHandler] Payment를 찾을 수 없습니다. paymentId=" + event.getPaymentId()
			));

		String transactionKey = resolveTransactionKey(payment);

		payment.markAsPartiallyCanceled(transactionKey, event.getCancelAmount(), CancelType.REFUND, "주문 취소");

		Cancel cancel = Cancel.create(
			payment.getId(),
			transactionKey,
			event.getCancelAmount(),
			"주문 취소",
			LocalDateTime.now()
		);
		cancelRepository.save(cancel);

		paymentRepository.save(payment);
		var domainEvents = payment.pullEvents();
		domainEvents.forEach(eventPublisher::publish);

		log.info("[OrderCancelEventHandler] 주문 취소 처리 완료. paymentId={}, status={}, refundedAmount={}",
			payment.getId(), payment.getStatus(), payment.getRefundedAmount());
	}

	private String resolveTransactionKey(Payment payment) {
		if (!payment.getMethod().requiresPg()) {
			return "internal-" + UUID.randomUUID();
		}
		throw new UnsupportedOperationException(
			"PG cancel not yet supported for method: " + payment.getMethod());
	}
}
