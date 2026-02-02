package app.giftify.payment.domain;

import app.giftify.shared.domain.vo.Money;

/**
 * 결제 시점의 주문 항목 정보.
 * Order BC에서 전달받아 Payment에 저장됩니다.
 */
public record OrderItemSnapshot(
	String orderItemId,
	String itemName,
	Money unitPrice,
	int quantity,
	Money subtotal,
	Long sellerId
) {
	public OrderItemSnapshot {
		if (orderItemId == null || orderItemId.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] orderItemId는 필수입니다.");
		}
		if (itemName == null || itemName.isBlank()) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] itemName은 필수입니다.");
		}
		if (unitPrice == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] unitPrice는 필수입니다.");
		}
		if (quantity <= 0) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] 수량은 1 이상이어야 합니다.");
		}
		if (subtotal == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] subtotal은 필수입니다.");
		}
		if (sellerId == null) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				"[OrderItemSnapshot] sellerId는 필수입니다.");
		}

		Money expectedSubtotal = unitPrice.times(quantity);
		if (!expectedSubtotal.equals(subtotal)) {
			throw new PaymentException(PaymentErrorCode.INVALID_INPUT_VALUE,
				String.format("[OrderItemSnapshot] subtotal(%s)이 price × quantity(%s)와 일치하지 않습니다.",
					subtotal, expectedSubtotal));
		}
	}
}
