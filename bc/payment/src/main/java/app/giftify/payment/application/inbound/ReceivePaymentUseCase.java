package app.giftify.payment.application.inbound;

public interface ReceivePaymentUseCase {
	void receive(ReceivePaymentCommand command);
}
