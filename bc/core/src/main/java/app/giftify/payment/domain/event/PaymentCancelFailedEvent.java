package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

public final class PaymentCancelFailedEvent extends PaymentInternalEvent {
	private final String orderNumber;
	private final String errorMetadata;
	private final LocalDateTime cancelFailedAt;

	public PaymentCancelFailedEvent(Long paymentId, String orderNumber, String errorMetadata, LocalDateTime cancelFailedAt) {
		super(paymentId);
		this.orderNumber = orderNumber;
		this.errorMetadata = errorMetadata;
		this.cancelFailedAt = cancelFailedAt;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public String getErrorMetadata() {
		return errorMetadata;
	}

	public LocalDateTime getCancelFailedAt() {
		return cancelFailedAt;
	}
}
