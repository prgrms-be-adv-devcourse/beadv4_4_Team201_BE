package app.giftify.payment.adapter.inbound.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.payment.adapter.outbound.jpa.JpaPaymentHistoryRepository;
import app.giftify.payment.adapter.outbound.jpa.entity.JpaPaymentHistory;
import app.giftify.payment.domain.PaymentEventType;
import app.giftify.payment.domain.PaymentHistory;
import app.giftify.payment.domain.PaymentHistoryKeyGenerator;
import app.giftify.payment.domain.event.PaymentCancelFailedEvent;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentFailedEvent;
import app.giftify.payment.domain.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class PaymentHistoryEventListener {
	private static final Logger log = LoggerFactory.getLogger(PaymentHistoryEventListener.class);


	private final JpaPaymentHistoryRepository historyRepository;

	@ApplicationModuleListener
	public void onPaymentSucceeded(PaymentSucceededEvent event) {
		saveHistory(event.data().paymentId(), event.data().orderNumber(),
			PaymentEventType.PAID, event.id(), toLocalDateTime(event.time()), null);
	}

	@ApplicationModuleListener
	public void onPaymentFailed(PaymentFailedEvent event) {
		saveHistory(event.data().paymentId(), event.data().orderNumber(),
			PaymentEventType.FAILED, event.id(), toLocalDateTime(event.time()), null);
	}

	@ApplicationModuleListener
	public void onPaymentCancelSucceeded(PaymentCanceledEvent event) {
		saveHistory(event.data().paymentId(), event.data().orderNumber(),
			PaymentEventType.CANCELED, event.id(), toLocalDateTime(event.time()), null);
	}

	@ApplicationModuleListener
	public void onPaymentCancelFailed(PaymentCancelFailedEvent event) {
		saveHistory(event.data().paymentId(), event.data().orderNumber(),
			PaymentEventType.CANCEL_FAILED, event.id(), toLocalDateTime(event.time()),
			event.data().errorMetadata());
	}

	private void saveHistory(Long paymentId, String orderNumber, PaymentEventType eventType,
		String eventId, LocalDateTime occurredAt, String metadata) {
		String historyKey = PaymentHistoryKeyGenerator.generate(orderNumber, eventType, eventId);
		PaymentHistory history = metadata != null
			? PaymentHistory.withMetadata(paymentId, historyKey, eventType, occurredAt, metadata)
			: PaymentHistory.create(paymentId, historyKey, eventType, occurredAt);
		historyRepository.save(JpaPaymentHistory.from(history, paymentId));

		log.debug("[PaymentHistoryEventListener] paymentId={}, eventType={}", paymentId, eventType);
	}

	private static LocalDateTime toLocalDateTime(Instant instant) {
		return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
}
