package domain.payment;

import java.time.LocalDateTime;

public record PaymentHistory(
	Long paymentId,
	PaymentEventType eventType,
	LocalDateTime occurredAt,
	String metadata // 추가 정보 - 이벤트마다 다른 추가 정보를 JSON으로 저장 (nullable)
) {

	public PaymentHistory(Long paymentId, PaymentEventType eventType, LocalDateTime occurredAt) {
		this(paymentId, eventType, occurredAt, null);
	}

	public static PaymentHistory withMetadata(
		Long paymentId,
		PaymentEventType eventType,
		LocalDateTime occurredAt,
		String metadata
	) {
		return new PaymentHistory(paymentId, eventType, occurredAt, metadata);
	}
}
