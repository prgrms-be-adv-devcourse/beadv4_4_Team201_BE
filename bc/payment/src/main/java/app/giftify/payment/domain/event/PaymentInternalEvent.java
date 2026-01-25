package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

/**
 * Payment BC 내부에서만 사용되는 도메인 이벤트.
 * 외부 BC로 발행되는 이벤트는 bc/shared의 PaymentExternalEvent를 사용합니다.
 */
public sealed interface PaymentInternalEvent
	permits PaymentPaidEvent,
			PaymentCanceledEvent,
			PaymentRefundedEvent,
			PaymentReceivedEvent { // FIXME BaseDomainEvent 상속하도록 바꿔야 함

	Long paymentId();

	LocalDateTime occurredAt();
}
