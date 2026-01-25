package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 환불 내부 이벤트.
 * PaymentInternalEventHandler에서 외부 이벤트로 변환됩니다.
 */
public record PaymentRefundedEvent( // FIXME BaseDomainEvent 상속하도록 바꿔야 함
	Long paymentId,
	Long memberId,
	String orderId,
	PaymentType paymentType,
	Money refundAmount,
	String reason,
	LocalDateTime occurredAt
) implements PaymentInternalEvent {
}
