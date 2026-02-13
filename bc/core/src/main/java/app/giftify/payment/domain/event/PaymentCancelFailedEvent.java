package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

public final class PaymentCancelFailedEvent extends PaymentInternalEvent {
	private final String orderId;
	private final String errorMetadata;
	private final LocalDateTime cancelFailedAt;

	public PaymentCancelFailedEvent(Long paymentId, String orderId, String errorMetadata, LocalDateTime cancelFailedAt) {
		super(paymentId);
		this.orderId = orderId;
		this.errorMetadata = errorMetadata;
		this.cancelFailedAt = cancelFailedAt;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getErrorMetadata() {
		return errorMetadata;
	}

	public LocalDateTime getCancelFailedAt() {
		return cancelFailedAt;
	}
}
