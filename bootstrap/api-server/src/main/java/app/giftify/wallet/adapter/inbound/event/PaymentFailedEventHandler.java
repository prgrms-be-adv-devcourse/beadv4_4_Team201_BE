package app.giftify.wallet.adapter.inbound.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.shared.domain.event.payment.PaymentFailedEvent;
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
public class PaymentFailedEventHandler {
	private static final Logger log = LoggerFactory.getLogger(PaymentFailedEventHandler.class);

    private final RestoreWalletUseCase restoreWalletUseCase;

    @ApplicationModuleListener
    public void handle(PaymentFailedEvent event) {
        // 복합결제가 아닌 경우 (일반 CARD 결제) — 지갑 복원 불필요
        if (!event.data().walletDeductedAmount().isGreaterThan(Money.zero())) {
            return;
        }

        log.info("[PaymentFailedEventHandler] PG 실패 → Wallet 복원. memberId={}, amount={}",
                event.data().memberId(), event.data().walletDeductedAmount());

        restoreWalletUseCase.restore(new RestoreWalletCommand(
                event.data().memberId(),
                event.data().walletDeductedAmount(),
                event.data().paymentId().toString(),
                TransactionType.PAYMENT_DEDUCT_COMPENSATION,
                ReferenceType.PAYMENT_COMPENSATION
        ));
    }
}
