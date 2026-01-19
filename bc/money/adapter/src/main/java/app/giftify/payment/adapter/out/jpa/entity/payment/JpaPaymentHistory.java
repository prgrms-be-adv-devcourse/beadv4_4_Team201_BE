package app.giftify.payment.adapter.out.jpa.entity.payment;

import java.time.LocalDateTime;

import app.giftify.support.jpa.BaseJpaHistoryEntity;
import domain.payment.PaymentEventType;
import domain.payment.PaymentHistory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "PAYMENT_PAYMENT_HISTORY")
public class JpaPaymentHistory extends BaseJpaHistoryEntity {

	@Column(nullable = false, updatable = false)
	private Long paymentId;

	@Column(updatable = false)
	private String idempotencyKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private PaymentEventType eventType;

	@Column(nullable = false, updatable = false)
	private LocalDateTime occurredAt;

	@Column(columnDefinition = "TEXT")
	private String metadata;

	protected JpaPaymentHistory() {
	}

	/**
	 * 도메인 객체로부터 JPA 엔티티 생성
	 */
	public JpaPaymentHistory(Long paymentId, PaymentHistory history) {
		this.paymentId = paymentId;
		this.idempotencyKey = history.idempotencyKey();
		this.eventType = history.eventType();
		this.occurredAt = history.occurredAt();
		this.metadata = history.metadata();
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
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
}
