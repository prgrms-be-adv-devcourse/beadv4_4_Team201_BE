package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class PaymentFailedEvent extends BaseDomainEvent {
	private final Long paymentId;
	private final String sourceType;
	private final Long userId;
	private final Money amount;
	private final PaymentType type;
	private final String reason;

	public PaymentFailedEvent(
		Long paymentId,
		String sourceType, Long userId,
		Money amount,
		PaymentType type,
		String reason,
		LocalDateTime occurredAt
	) {
		super(occurredAt);
		this.paymentId = paymentId;
		this.sourceType = sourceType;
		this.userId = userId;
		this.amount = amount;
		this.type = type;
		this.reason = reason;
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public Long getUserId() {
		return userId;
	}

	public Money getAmount() {
		return amount;
	}

	public PaymentType getType() {
		return type;
	}

	public String getReason() {
		return reason;
	}

	public LocalDateTime getFailedAt() {
		return super.getOccurredAt();
	}

	@Override
	public String toString() {
		return "PaymentFailedEvent{" +
			"paymentId=" + paymentId +
			", sourceType='" + sourceType + "'" +
			", userId=" + userId +
			", amount=" + amount +
			", type=" + type +
			", reason='" + reason + "'" +
			", eventId='" + getEventId() + "'" +
			", failedAt=" + getOccurredAt() +
			'}';
	}
}
