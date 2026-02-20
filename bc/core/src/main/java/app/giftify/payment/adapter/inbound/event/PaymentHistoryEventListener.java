package app.giftify.payment.adapter.inbound.event;

import java.time.LocalDateTime;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.payment.adapter.outbound.jpa.JpaPaymentHistoryRepository;
import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;
import app.giftify.payment.domain.PaymentEventType;
import app.giftify.payment.domain.PaymentHistory;
import app.giftify.payment.domain.PaymentHistoryKeyGenerator;
import app.giftify.payment.domain.event.PaymentCancelFailedEvent;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentConfirmedEvent;
import app.giftify.payment.domain.event.PaymentFailedEvent;
import app.giftify.payment.domain.event.PaymentReceivedEvent;
import app.giftify.payment.domain.event.PaymentRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentHistoryEventListener {

	private final JpaPaymentHistoryRepository historyRepository;

	@ApplicationModuleListener
	public void onPaymentConfirmed(PaymentConfirmedEvent event) {
		saveHistory(event.getPaymentId(), event.getOrderNumber(),
			PaymentEventType.PAID, event.getEventId(), event.getOccurredAt(), null);
	}

	@ApplicationModuleListener
	public void onPaymentCanceled(PaymentCanceledEvent event) {
		saveHistory(event.getPaymentId(), event.getOrderNumber(),
			PaymentEventType.CANCELED, event.getEventId(), event.getOccurredAt(), null);
	}

	@ApplicationModuleListener
	public void onPaymentRefunded(PaymentRefundedEvent event) {
		saveHistory(event.getPaymentId(), event.getOrderNumber(),
			PaymentEventType.REFUNDED, event.getEventId(), event.getOccurredAt(), null);
	}

	@ApplicationModuleListener
	public void onPaymentReceived(PaymentReceivedEvent event) {
		saveHistory(event.getPaymentId(), event.getOrderNumber(),
			PaymentEventType.RECEIVED, event.getEventId(), event.getOccurredAt(), null);
	}

	@ApplicationModuleListener
	public void onPaymentFailed(PaymentFailedEvent event) {
		saveHistory(event.getPaymentId(), event.getOrderNumber(),
			PaymentEventType.FAILED, event.getEventId(), event.getOccurredAt(), null);
	}

	@ApplicationModuleListener
	public void onPaymentCancelFailed(PaymentCancelFailedEvent event) {
		saveHistory(event.getPaymentId(), event.getOrderNumber(),
			PaymentEventType.CANCEL_FAILED, event.getEventId(), event.getOccurredAt(),
			event.getErrorMetadata());
	}

	private void saveHistory(Long paymentId, String orderNumber, PaymentEventType eventType,
		String eventId, LocalDateTime occurredAt, String metadata) {
		String historyKey = PaymentHistoryKeyGenerator.generate(orderNumber, eventType, eventId);
		PaymentHistory history = metadata != null
			? PaymentHistory.withMetadata(paymentId, historyKey, eventType, occurredAt, metadata)
			: PaymentHistory.create(paymentId, historyKey, eventType, occurredAt);
		historyRepository.save(JpaPaymentHistory.from(history, paymentId));

		log.debug("[PaymentHistoryEventListener] 히스토리 저장 완료. paymentId={}, eventType={}",
			paymentId, eventType);
	}
}
