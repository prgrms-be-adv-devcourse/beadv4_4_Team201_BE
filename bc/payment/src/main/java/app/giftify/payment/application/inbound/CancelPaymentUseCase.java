package app.giftify.payment.application.inbound;

public interface CancelPaymentUseCase {
	void cancel(CancelPaymentCommand command);
}
