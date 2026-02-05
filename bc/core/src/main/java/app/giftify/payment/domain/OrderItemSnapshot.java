package app.giftify.payment.domain;

import app.giftify.shared.domain.vo.Money;

/**
 * 결제 시점의 펀딩 참여 정보 스냅샷.
 *
 * @param targetId 펀딩 ID (Order의 targetId와 동일)
 * @param amount   참여 금액 (Order의 amount와 동일)
 * @param sellerId 정산 대상 (환불 시 Settlement BC에 전달)
 */
public record OrderItemSnapshot(
	Long targetId,
	Money amount,
	Long sellerId
) {
	public OrderItemSnapshot {
		if (targetId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] targetId는 필수입니다.");
		}
		if (amount == null || amount.isLessThanOrEqual(Money.zero())) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] amount는 0보다 커야 합니다.");
		}
		if (sellerId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] sellerId는 필수입니다.");
		}
	}
}
