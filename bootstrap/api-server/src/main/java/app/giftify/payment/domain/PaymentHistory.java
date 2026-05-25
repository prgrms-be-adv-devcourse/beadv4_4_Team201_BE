package app.giftify.payment.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import app.giftify.support.common.base.BaseDomainModel;

public class PaymentHistory extends BaseDomainModel {

	private final Long paymentId;
	private final String historyKey;
	private final PaymentEventType eventType;
	private final LocalDateTime occurredAt;
	private final String metadata;

	private PaymentHistory(
		Long id,
		Long paymentId,
		String historyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		super(id);
		this.paymentId = paymentId;
		this.historyKey = historyKey;
		this.eventType = eventType;
		this.occurredAt = occurredAt;
		this.metadata = metadata;
	}

	// ========== 정적 팩토리 메서드 ========== //

	public static PaymentHistory create(
		Long paymentId,
		String historyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt
	) {
		return new PaymentHistory(null, paymentId, historyKey, eventType, occurredAt, null);
	}

	public static PaymentHistory withMetadata(
		Long paymentId,
		String historyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		return new PaymentHistory(null, paymentId, historyKey, eventType, occurredAt, metadata);
	}

	public static PaymentHistory restore(
		Long id,
		Long paymentId,
		String historyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		return new PaymentHistory(id, paymentId, historyKey, eventType, occurredAt, metadata);
	}

	// ========== Getter ========== //

	public Long getPaymentId() {
		return paymentId;
	}

	public String getHistoryKey() {
		return historyKey;
	}

	public PaymentEventType getEventType() {
		return eventType;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public String getMetadata() {
		return metadata;
	}

	// ========== equals / hashCode ========== //

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PaymentHistory that))
			return false;
		if (getId() != null && that.getId() != null) {
			return Objects.equals(getId(), that.getId());
		}
		return Objects.equals(historyKey, that.historyKey)
			&& Objects.equals(eventType, that.eventType)
			&& Objects.equals(occurredAt, that.occurredAt);
	}

	@Override
	public int hashCode() {
		if (getId() != null) {
			return Objects.hash(getId());
		}
		return Objects.hash(historyKey, eventType, occurredAt);
	}
}
