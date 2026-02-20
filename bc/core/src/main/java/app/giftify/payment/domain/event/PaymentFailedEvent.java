package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

public final class PaymentFailedEvent extends PaymentInternalEvent {
	private final String orderNumber;
	private final LocalDateTime failedAt;

	public PaymentFailedEvent(Long paymentId, String orderNumber, LocalDateTime failedAt) {
		super(paymentId);
		this.orderNumber = orderNumber;
		this.failedAt = failedAt;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public LocalDateTime getFailedAt() {
		return failedAt;
	}
}
