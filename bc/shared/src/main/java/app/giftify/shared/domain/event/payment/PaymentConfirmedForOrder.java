package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import app.giftify.shared.domain.vo.Money;

/**
 * Order BC로 발행되는 결제 확인 이벤트.
 *
 * <p>결제가 성공적으로 완료되었을 때 Order BC에 알려
 * 주문 상태를 업데이트할 수 있도록 합니다.</p>
 *
 * @param paymentId  결제 ID
 * @param eventId    이벤트 고유 ID (멱등성 보장)
 * @param occurredAt 이벤트 발생 시각
 * @param orderId    주문 ID (Order BC의 대체키)
 * @param amount     결제 금액
 */
public record PaymentConfirmedForOrder(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	String orderId,
	Money amount
) implements PaymentExternalEvent {

	public static PaymentConfirmedForOrder create(
		Long paymentId,
		String orderId,
		Money amount,
		LocalDateTime occurredAt
	) {
		return new PaymentConfirmedForOrder(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			orderId,
			amount
		);
	}
}
