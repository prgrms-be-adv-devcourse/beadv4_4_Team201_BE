package payment.usecase.result;

import app.giftify.shared.domain.vo.Money;
import domain.payment.PaymentStatus;

public record PaymentResult(
	Long paymentId,
	PaymentStatus status,
	Money amount
) {
}
