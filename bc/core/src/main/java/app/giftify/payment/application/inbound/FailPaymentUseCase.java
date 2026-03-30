package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.Payment;

public interface FailPaymentUseCase {
	void fail(Payment payment);
}
