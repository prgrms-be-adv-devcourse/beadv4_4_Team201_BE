package payment.usecase;

import payment.usecase.command.RefundPaymentCommand;

public interface PaymentRefundUseCase {
    void refund(RefundPaymentCommand command);
}
