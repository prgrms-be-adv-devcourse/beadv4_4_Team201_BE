package app.giftify.payment.domain;

import java.time.LocalDateTime;

import app.giftify.shared.domain.base.BaseDomainModel;

/**
 * 결제 이력 이벤트.
 * 결제 상태 변경 시마다 기록됩니다.
 */
public class PaymentHistory extends BaseDomainModel {

	private final Long paymentId;
	private final String idempotencyKey;
	private final PaymentEventType eventType;
	private final LocalDateTime occurredAt;
	private final String metadata;

	private PaymentHistory(
		Long id,
		Long paymentId,
		String idempotencyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		super(id);
		this.paymentId = paymentId;
		this.idempotencyKey = idempotencyKey;
		this.eventType = eventType;
		this.occurredAt = occurredAt;
		this.metadata = metadata;
	}

	// ========== 정적 팩토리 메서드 ========== //

	/**
	 * 새로운 이력 생성 (id 없음 - 영속화 전)
	 */
	public static PaymentHistory create(
		Long paymentId,
		String idempotencyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt
	) {
		return new PaymentHistory(null, paymentId, idempotencyKey, eventType, occurredAt, null);
	}

	/**
	 * 메타데이터와 함께 새로운 이력 생성
	 */
	public static PaymentHistory withMetadata(
		Long paymentId,
		String idempotencyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		return new PaymentHistory(null, paymentId, idempotencyKey, eventType, occurredAt, metadata);
	}

	/**
	 * 영속화된 이력 복원 (id 포함)
	 */
	public static PaymentHistory restore(
		Long id,
		Long paymentId,
		String idempotencyKey,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		return new PaymentHistory(id, paymentId, idempotencyKey, eventType, occurredAt, metadata);
	}

	// ========== Getter ========== //

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
