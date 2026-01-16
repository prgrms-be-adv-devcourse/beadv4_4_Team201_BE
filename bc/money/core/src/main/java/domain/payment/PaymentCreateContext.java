package domain.payment;

import app.giftify.shared.domain.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentCreateContext(
	Long userId,
	Money amount,
	Long fundingId,
	PaymentType type
) {
}
