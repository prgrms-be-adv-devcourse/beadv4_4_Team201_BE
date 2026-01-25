package app.giftify.payment.application.inbound;

public interface ConfirmPaymentUseCase {
	void confirm(ConfirmPaymentCommand command);
}
