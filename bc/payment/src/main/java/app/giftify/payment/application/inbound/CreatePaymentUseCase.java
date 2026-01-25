package app.giftify.payment.application.inbound;

public interface CreatePaymentUseCase {
	PaymentCreatedResult create(CreatePaymentCommand command);
}
