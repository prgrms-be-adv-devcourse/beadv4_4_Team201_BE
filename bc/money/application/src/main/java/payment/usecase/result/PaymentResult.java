package payment.usecase.result;

import domain.payment.PaymentStatus;

public record PaymentResult(
	Long paymentId,
	PaymentStatus status
) {
}
