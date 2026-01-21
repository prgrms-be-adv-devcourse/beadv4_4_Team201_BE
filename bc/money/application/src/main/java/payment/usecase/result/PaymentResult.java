package payment.usecase.result;

import app.giftify.shared.domain.vo.Money;
import domain.payment.PaymentStatus;

public record PaymentResult(
	Long paymentId,
	String orderId,
	PaymentStatus status,
	Money amount
) {
}
