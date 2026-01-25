package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 완료 내부 이벤트.
 * PaymentInternalEventHandler에서 외부 이벤트로 변환됩니다.
 */
public record PaymentPaidEvent( // FIXME BaseDomainEvent 상속하도록 바꿔야 함
	Long paymentId,
	Long memberId,
	String orderId,
	PaymentType paymentType,
	Money paidAmount,
	LocalDateTime occurredAt
) implements PaymentInternalEvent {
}
