package app.giftify.application.inbound;

public interface RefundPaymentUseCase {
	void refund(RefundPaymentCommand command);
}
