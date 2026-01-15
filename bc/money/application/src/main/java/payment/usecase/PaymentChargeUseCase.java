package payment.usecase;

import payment.usecase.command.PaymentChargeCommand;
import payment.usecase.result.PaymentResult;

public interface PaymentChargeUseCase {
	PaymentResult charge(PaymentChargeCommand command);
}
