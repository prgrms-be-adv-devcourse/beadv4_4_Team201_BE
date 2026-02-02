package app.giftify.payment.application.inbound;

public interface ConfirmPaymentUseCase {
	ConfirmPaymentResult confirm(ConfirmPaymentCommand command);
}
