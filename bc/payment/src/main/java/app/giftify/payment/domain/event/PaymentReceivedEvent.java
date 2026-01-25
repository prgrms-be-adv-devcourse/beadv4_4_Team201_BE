package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

/**
 * 수령 확정 내부 이벤트.
 */
public record PaymentReceivedEvent( // FIXME BaseDomainEvent 상속하도록 바꿔야 함
	Long paymentId,
	Long memberId,
	String orderId,
	LocalDateTime occurredAt
) implements PaymentInternalEvent {
}
