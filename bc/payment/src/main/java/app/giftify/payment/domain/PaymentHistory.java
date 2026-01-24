package app.giftify.payment.domain;

import java.time.LocalDateTime;

/**
 * 결제 이력 이벤트.
 * 결제 상태 변경 시마다 기록됩니다.
 */
public record PaymentHistory(
	Long id, // 이력 고유 식별자
	Long paymentId,
	String idempotencyKey,
	PaymentEventType eventType,
	LocalDateTime occurredAt,
	String metadata // 추가 정보 - 이벤트마다 다른 추가 정보를 JSON으로 저장 (nullable)
) {

	/**
	 * 새로운 이력 생성 (id 없음 - 영속화 전)
	 */
	public PaymentHistory(Long paymentId, String idempotencyKey, PaymentEventType eventType, LocalDateTime occurredAt) {
		this(null, paymentId, idempotencyKey, eventType, occurredAt, null);
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
}
