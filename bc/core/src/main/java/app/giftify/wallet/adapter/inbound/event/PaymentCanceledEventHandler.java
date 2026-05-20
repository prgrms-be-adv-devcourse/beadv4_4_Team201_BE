package app.giftify.wallet.adapter.inbound.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.application.inbound.RestoreWalletCommand;
import app.giftify.wallet.application.inbound.RestoreWalletUseCase;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCanceledEventHandler {
	private static final Logger log = LoggerFactory.getLogger(PaymentCanceledEventHandler.class);

    private final RestoreWalletUseCase restoreWalletUseCase;

    @ApplicationModuleListener
    public void handle(PaymentCanceledEvent event) {
        if (!event.data().walletDeductedAmount().isGreaterThan(Money.zero())) { //  walletDeductedAmount > 0이면 복원 (순수 CARD는 0이라 skip)
            return;
        }

        log.info("[PaymentCanceledEventHandler] Wallet 결제 취소 → 잔액 복원. memberId={}, cancelAmount={}",
                event.data().memberId(), event.data().cancelAmount());

        restoreWalletUseCase.restore(new RestoreWalletCommand(
                event.data().memberId(),
                event.data().walletDeductedAmount(),
                event.data().transactionKey(),
                TransactionType.CANCEL_REFUND,
                ReferenceType.CANCEL
        ));
    }
}
