package domain.payment;

import app.giftify.shared.domain.event.payment.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentCreateContext(
	Long userId,
	Money amount,
	PaymentType type
) {
}
