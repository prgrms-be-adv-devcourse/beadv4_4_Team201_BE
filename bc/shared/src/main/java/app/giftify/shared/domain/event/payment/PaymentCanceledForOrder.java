package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order BC로 발행되는 결제 취소 이벤트.
 *
 * <p>결제가 취소되었을 때 Order BC에 알려
 * 주문 상태를 취소로 변경할 수 있도록 합니다.</p>
 *
 * @param paymentId  결제 ID
 * @param eventId    이벤트 고유 ID
 * @param occurredAt 이벤트 발생 시각
 * @param orderId    주문 ID
 * @param reason     취소 사유
 */
public record PaymentCanceledForOrder(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	String orderId,
	String reason
) implements PaymentExternalEvent {

	public static PaymentCanceledForOrder create(
		Long paymentId,
		String orderId,
		String reason,
		LocalDateTime occurredAt
	) {
		return new PaymentCanceledForOrder(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			orderId,
			reason
		);
	}
}
