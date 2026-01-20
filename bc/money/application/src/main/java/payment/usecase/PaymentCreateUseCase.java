package payment.usecase;

import payment.usecase.command.CreatePaymentCommand;
import payment.usecase.result.PaymentResult;

public interface PaymentCreateUseCase { // TODO 명확한 의미를 담도록 이름 변경 필요
	PaymentResult create(CreatePaymentCommand command);
}
