package app.giftify.payment.application.inbound;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.event.PaymentCanceledEvent;
import app.giftify.payment.domain.event.PaymentPaidEvent;
import app.giftify.payment.domain.event.PaymentRefundedEvent;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.payment.PaymentCanceledForOrder;
import app.giftify.shared.domain.event.payment.PaymentCompletedForFunding;
import app.giftify.shared.domain.event.payment.PaymentConfirmedForOrder;
import app.giftify.shared.domain.event.payment.PaymentRefundedForSettlement;
import app.giftify.shared.domain.type.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment 내부 이벤트를 수신하여 외부 BC 전용 이벤트로 발행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {
	private final EventPublisher eventPublisher;
	private final PaymentRepository paymentRepository;

	@EventListener
	public void handle(PaymentPaidEvent event) {
		log.info("[PaymentEventHandler] 결제 완료 이벤트 수신. paymentId={}", event.getPaymentId());

		if (event.getPaymentType() == PaymentType.FUNDING) {
			// Funding BC용 이벤트 - orderId를 그대로 전달 (Funding BC가 펀딩 식별 책임)
			eventPublisher.publish(PaymentCompletedForFunding.create(
				event.getPaymentId(),
				event.getOrderId(),
				event.getMemberId(),
				event.getPaidAmount(),
				event.getOccurredAt()
			));
		} else {
			// 기본: Order BC용 이벤트 (POINT_CHARGE 등)
			eventPublisher.publish(PaymentConfirmedForOrder.create(
				event.getPaymentId(),
				event.getOrderId(),
				event.getPaidAmount(),
				event.getOccurredAt()
			));
		}
	}

	@EventListener
	public void handle(PaymentCanceledEvent event) {
		log.info("[PaymentEventHandler] 결제 취소 이벤트 수신. paymentId={}", event.getPaymentId());

		eventPublisher.publish(PaymentCanceledForOrder.create(
			event.getPaymentId(),
			event.getOrderId(),
			event.getReason(),
			event.getOccurredAt()
		));
	}

	@EventListener
	public void handle(PaymentRefundedEvent event) {
		log.info("[PaymentEventHandler] 결제 환불 이벤트 수신. paymentId={}", event.getPaymentId());

		Payment payment = paymentRepository.findById(event.getPaymentId())
			.orElseThrow(() -> new IllegalStateException(
				"[PaymentEventHandler] Payment not found: " + event.getPaymentId()));

		List<Long> sellerIds = payment.getOrderItems().stream()
			.map(item -> item.sellerId())
			.distinct()
			.toList();

		eventPublisher.publish(PaymentRefundedForSettlement.create(
			event.getPaymentId(),
			event.getRefundAmount(),
			sellerIds,
			event.getOccurredAt()
		));
	}
}
