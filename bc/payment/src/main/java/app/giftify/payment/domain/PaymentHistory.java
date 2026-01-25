package app.giftify.payment.domain;

import java.time.LocalDateTime;
import java.util.Objects;

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

	// ========== equals / hashCode ========== //

	/**
	 * Entity 동일성은 id로 판단합니다.
	 * id가 null인 경우(비영속 상태)에는 복합 비즈니스 키
	 * (idempotencyKey, eventType, occurredAt)를 비교합니다.
	 *
	 * <p>같은 Payment의 여러 이벤트(PAID, REFUNDED 등)가 동일한 idempotencyKey를
	 * 가지므로, eventType과 occurredAt도 함께 비교해야 정확한 동일성 판단이 가능합니다.</p>
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PaymentHistory that))
			return false;

		// id가 있으면 id로 비교
		if (getId() != null && that.getId() != null) {
			return Objects.equals(getId(), that.getId());
		}

		// 비영속 상태에서는 복합 비즈니스 키로 비교
		return Objects.equals(idempotencyKey, that.idempotencyKey)
			&& Objects.equals(eventType, that.eventType)
			&& Objects.equals(occurredAt, that.occurredAt);
	}

	@Override
	public int hashCode() {
		// id가 있으면 id 기반, 없으면 복합 비즈니스 키 기반
		if (getId() != null) {
			return Objects.hash(getId());
		}
		return Objects.hash(idempotencyKey, eventType, occurredAt);
	}
}
