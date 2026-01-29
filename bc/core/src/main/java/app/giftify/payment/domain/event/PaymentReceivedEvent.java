package app.giftify.payment.domain.event;

import java.time.LocalDateTime;

/**
 * 수령 확정 내부 이벤트.
 */
public final class PaymentReceivedEvent extends PaymentInternalEvent {
	private final Long memberId;
	private final String orderId;
	private final LocalDateTime receivedAt;

	public PaymentReceivedEvent(
		Long paymentId,
		Long memberId,
		String orderId,
		LocalDateTime receivedAt
	) {
		super(paymentId);
		this.memberId = memberId;
		this.orderId = orderId;
		this.receivedAt = receivedAt;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getOrderId() {
		return orderId;
	}

	public LocalDateTime getReceivedAt() {
		return receivedAt;
	}
}
