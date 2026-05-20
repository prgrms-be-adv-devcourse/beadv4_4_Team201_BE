package app.giftify.payment.application.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class CompositePaymentCreateStrategy implements PaymentCreateStrategy {
	private static final Logger log = LoggerFactory.getLogger(CompositePaymentCreateStrategy.class);


    private final DeductWalletUseCase deductWalletUseCase;

    @Override
    public boolean canHandle(CreatePaymentCommand command) {
        return !command.method().isWalletPayment()
                && command.walletDeductAmount().isGreaterThan(Money.zero());
    }

    @Override
    public PaymentCreatedResult execute(Payment savedPayment, CreatePaymentCommand command) {
        DeductWalletCommand deductCommand = new DeductWalletCommand(
                command.memberId(),
                savedPayment.getId(),
                command.orderNumber(),
                command.walletDeductAmount()
        );

        DeductWalletResult result = deductWalletUseCase.deductForPayment(deductCommand);

        if (!result.success()) {
            log.warn("[Payment] 복합결제 예치금 잔액 부족. paymentId={}, required={}, current={}",
                    savedPayment.getId(), result.requiredAmount(), result.currentBalance());

            throw new PaymentException(
                    PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
                    String.format("필요 금액: %s, 현재 잔액: %s",
                            result.requiredAmount(), result.currentBalance())
            );
        }

        log.info("[Payment] 복합결제 예치금 차감 완료. paymentId={}, walletDeduct={}, pgAmount={}",
                savedPayment.getId(), command.walletDeductAmount(),
                command.expectedAmount().minus(command.walletDeductAmount()));

        return PaymentCreatedResult.from(savedPayment);
    }
}
