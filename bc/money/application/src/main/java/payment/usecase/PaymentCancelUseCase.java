package payment.usecase;

import payment.usecase.command.CancelPaymentCommand;

public interface PaymentCancelUseCase {
	void cancel(CancelPaymentCommand command);
}
