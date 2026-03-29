package app.giftify.payment.application.strategy;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.Payment;

public interface PaymentCreateStrategy {

    boolean canHandle(CreatePaymentCommand command);

    PaymentCreatedResult execute(Payment savedPayment, CreatePaymentCommand command);
}
