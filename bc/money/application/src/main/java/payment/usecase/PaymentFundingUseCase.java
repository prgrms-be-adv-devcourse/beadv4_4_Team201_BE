package payment.usecase;

import payment.usecase.command.PaymentFundingCommand;
import payment.usecase.result.PaymentResult;

public interface PaymentFundingUseCase {
	PaymentResult payForFunding(PaymentFundingCommand command);
}
