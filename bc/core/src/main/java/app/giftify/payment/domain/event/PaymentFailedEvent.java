package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

public final class PaymentFailedEvent extends PaymentInternalEvent {
	private final String orderId;
	private final LocalDateTime failedAt;

	public PaymentFailedEvent(Long paymentId, String orderId, LocalDateTime failedAt) {
		super(paymentId);
		this.orderId = orderId;
		this.failedAt = failedAt;
	}

	public String getOrderId() {
		return orderId;
	}

	public LocalDateTime getFailedAt() {
		return failedAt;
	}
}
