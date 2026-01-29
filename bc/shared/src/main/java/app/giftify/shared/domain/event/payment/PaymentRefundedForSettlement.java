package app.giftify.shared.domain.event.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.giftify.shared.domain.vo.Money;

/**
 * Settlement BC로 발행되는 환불 이벤트.
 *
 * <p>환불이 발생했을 때 Settlement BC에 알려
 * 정산 금액을 조정할 수 있도록 합니다.</p>
 *
 * @param paymentId    결제 ID
 * @param eventId      이벤트 고유 ID
 * @param occurredAt   이벤트 발생 시각
 * @param refundAmount 환불 금액
 * @param sellerIds    영향받는 판매자 ID 목록
 */
public record PaymentRefundedForSettlement(
	Long paymentId,
	String eventId,
	LocalDateTime occurredAt,
	Money refundAmount,
	List<Long> sellerIds
) implements PaymentExternalEvent {

	public PaymentRefundedForSettlement {
		sellerIds = sellerIds != null ? List.copyOf(sellerIds) : List.of();
	}

	public static PaymentRefundedForSettlement create(
		Long paymentId,
		Money refundAmount,
		List<Long> sellerIds,
		LocalDateTime occurredAt
	) {
		return new PaymentRefundedForSettlement(
			paymentId,
			UUID.randomUUID().toString(),
			occurredAt,
			refundAmount,
			sellerIds
		);
	}
}
