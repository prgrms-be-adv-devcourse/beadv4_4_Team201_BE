package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;

/**
 * 외부 BC로 발행되는 Payment 이벤트의 sealed interface.
 * Payment BC의 내부 이벤트와 분리하여 BC 간 계약을 명확히 합니다.
 *
 * <p>내부 이벤트는 {@code bc/payment/domain/event/PaymentInternalEvent}를 사용합니다.</p>
 */
public sealed interface PaymentExternalEvent
	permits PaymentPaidExternalEvent,
			PaymentCanceledExternalEvent,
			PaymentRefundedExternalEvent {

	/**
	 * 결제 ID
	 */
	Long paymentId();

	/**
	 * 이벤트 고유 식별자 (멱등성 보장용)
	 */
	String eventId();

	/**
	 * 이벤트 발생 시각
	 */
	LocalDateTime occurredAt();
}
