package app.giftify.application.inbound;

public interface CreatePaymentUseCase {
	PaymentCreatedResult create(CreatePaymentCommand command);
}
