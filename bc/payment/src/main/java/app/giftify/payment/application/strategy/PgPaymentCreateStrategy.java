package app.giftify.payment.application.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.Payment;
import app.giftify.shared.domain.vo.Money;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class PgPaymentCreateStrategy implements PaymentCreateStrategy {
	private static final Logger log = LoggerFactory.getLogger(PgPaymentCreateStrategy.class);


    @Override
    public boolean canHandle(CreatePaymentCommand command) {
        return !command.method().isWalletPayment()
                && !command.walletDeductAmount().isGreaterThan(Money.zero());
    }

    @Override
    public PaymentCreatedResult execute(Payment savedPayment, CreatePaymentCommand command) {
        log.info("[Payment] 펀딩 결제 생성. paymentId={}, orderId={}, amount={}",
                savedPayment.getId(), savedPayment.getOrderNumber(), command.expectedAmount());

        return PaymentCreatedResult.from(savedPayment);
    }
}
