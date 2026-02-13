package app.giftify.payment.domain.event;

import app.giftify.shared.domain.event.BaseDomainEvent;

/**
 * Payment BC 내부에서만 사용되는 도메인 이벤트.
 * 외부 BC로 발행되는 이벤트는 bc/shared의 PaymentExternalEvent를 사용합니다.
 */
public abstract sealed class PaymentInternalEvent extends BaseDomainEvent
	permits PaymentConfirmedEvent,
			PaymentCanceledEvent,
			PaymentRefundedEvent,
			PaymentReceivedEvent,
			PaymentFailedEvent,
			PaymentCancelFailedEvent {

	private final Long paymentId;

	protected PaymentInternalEvent(Long paymentId) {
		super();
		this.paymentId = paymentId;
	}

	public Long getPaymentId() {
		return paymentId;
	}
}
