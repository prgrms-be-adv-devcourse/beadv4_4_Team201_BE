package app.giftify.application.inbound;

public interface ReceivePaymentUseCase {
	void receive(ReceivePaymentCommand command);
}
