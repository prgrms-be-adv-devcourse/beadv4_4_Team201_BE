package payment.usecase;

import payment.usecase.command.CreatePaymentCommand;
import payment.usecase.result.PaymentResult;

public interface PaymentCreateUseCase {
	PaymentResult create(CreatePaymentCommand command);
}
