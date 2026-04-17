package app.giftify.payment.application.strategy;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.Payment;
import app.giftify.shared.domain.vo.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(3)
@Component
public class PgPaymentCreateStrategy implements PaymentCreateStrategy {

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
