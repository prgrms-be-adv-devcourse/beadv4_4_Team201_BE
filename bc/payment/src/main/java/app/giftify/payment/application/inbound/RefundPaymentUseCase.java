package app.giftify.payment.application.inbound;

public interface RefundPaymentUseCase {
	void refund(RefundPaymentCommand command);
}
