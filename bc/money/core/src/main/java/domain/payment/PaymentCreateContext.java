package domain.payment;

import app.giftify.shared.domain.type.PaymentType;
import app.giftify.shared.domain.vo.Money;

public record PaymentCreateContext(
	Long userId,
	Money amount,
	PaymentType type
) {
}
