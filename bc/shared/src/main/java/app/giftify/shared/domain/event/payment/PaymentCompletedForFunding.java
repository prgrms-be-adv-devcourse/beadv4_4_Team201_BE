package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import app.giftify.shared.domain.vo.Money;

/**
 * Funding BC로 발행되는 결제 완료 이벤트.
 *
 * <p>펀딩 참여 결제가 완료되었을 때 Funding BC에 알려
 * 펀딩 진행 상황을 업데이트할 수 있도록 합니다.</p>
 *
 * <p>Payment BC는 orderId가 무엇을 의미하는지 알지 못합니다.
 * Funding BC가 이 이벤트를 수신하여 orderId로 해당 펀딩을 조회해야 합니다.</p>
 *
 * @param paymentId     결제 ID
 * @param eventId       이벤트 고유 ID
 * @param occurredAt    이벤트 발생 시각
 * @param orderId       주문 ID (Funding BC에서 펀딩 식별에 사용)
 * @param participantId 참여자 ID (회원 ID)
 * @param amount        결제 금액
 */
public record PaymentCompletedForFunding(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	String orderId,
	Long participantId,
	Money amount
) implements PaymentExternalEvent {

	public static PaymentCompletedForFunding create(
		Long paymentId,
		String orderId,
		Long participantId,
		Money amount,
		LocalDateTime occurredAt
	) {
		return new PaymentCompletedForFunding(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			orderId,
			participantId,
			amount
		);
	}
}
