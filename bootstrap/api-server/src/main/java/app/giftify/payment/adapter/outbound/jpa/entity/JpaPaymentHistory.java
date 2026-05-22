package app.giftify.payment.adapter.outbound.jpa.entity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import app.giftify.payment.domain.PaymentHistory;
import app.giftify.payment.domain.PaymentEventType;
import app.giftify.support.jpa.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_histories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaPaymentHistory extends BaseJpaEntity {

	@Column(name = "payment_id", nullable = false)
	private Long paymentId;

	@Column(name = "history_key", unique = true, nullable = false, length = 255)
	private String historyKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 50)
	private PaymentEventType eventType;

	@Column(name = "occurred_at", nullable = false)
	private LocalDateTime occurredAt;

	@Column(name = "metadata", columnDefinition = "TEXT")
	private String metadata;

	private JpaPaymentHistory(
		Long paymentId,
		String historyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		this.paymentId = paymentId;
		this.historyKey = historyKey;
		this.eventType = eventType;
		this.occurredAt = occurredAt;
		this.metadata = metadata;
	}

	public static JpaPaymentHistory from(PaymentHistory history, Long paymentId) {
		return new JpaPaymentHistory(
			paymentId,
			history.getHistoryKey(),
			history.getEventType(),
			history.getOccurredAt(),
			history.getMetadata()
		);
	}

	public PaymentHistory toDomain() {
		return PaymentHistory.restore(
			super.getId(),
			paymentId,
			historyKey,
			eventType,
			occurredAt,
			metadata
		);
	}
}
