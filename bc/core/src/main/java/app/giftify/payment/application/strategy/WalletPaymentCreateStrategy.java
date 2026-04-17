package app.giftify.payment.application.strategy;

import app.giftify.payment.application.inbound.CreatePaymentCommand;
import app.giftify.payment.application.inbound.PaymentCreatedResult;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.wallet.application.inbound.DeductWalletCommand;
import app.giftify.wallet.application.inbound.DeductWalletResult;
import app.giftify.wallet.application.inbound.DeductWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class WalletPaymentCreateStrategy implements PaymentCreateStrategy {

    private final DeductWalletUseCase deductWalletUseCase;

    @Override
    public boolean canHandle(CreatePaymentCommand command) {
        return command.method().isWalletPayment()
                || command.walletDeductAmount().equals(command.expectedAmount());
    }

    @Override
    public PaymentCreatedResult execute(Payment savedPayment, CreatePaymentCommand command) {
        DeductWalletCommand deductCommand = new DeductWalletCommand(
                command.memberId(),
                savedPayment.getId(),
                command.orderNumber(),
                command.expectedAmount()
        );

        DeductWalletResult result = deductWalletUseCase.deductForPayment(deductCommand);

        if (!result.success()) {
            log.warn("[Payment] 예치금 잔액 부족. paymentId={}, required={}, current={}",
                    savedPayment.getId(), result.requiredAmount(), result.currentBalance());

            throw new PaymentException(
                    PaymentErrorCode.INSUFFICIENT_WALLET_BALANCE,
                    String.format("필요 금액: %s, 현재 잔액: %s",
                            result.requiredAmount(), result.currentBalance())
            );
        }

        log.info("[Payment] 예치금 결제 요청 완료. paymentId={}, walletId={}",
                savedPayment.getId(), result.walletId());

        return PaymentCreatedResult.from(savedPayment);
    }
}
