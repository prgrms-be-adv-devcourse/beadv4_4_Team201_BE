package app.giftify.payment.adapter.in.event;

import app.giftify.shared.domain.event.wallet.WalletChargeCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import walletHistory.recoder.WalletHistoryRecoder;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Component
@RequiredArgsConstructor
public class WalletHistoryEventListener {

    private final WalletHistoryRecoder walletHistoryRecoder;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    @Transactional(propagation = REQUIRES_NEW)
    public void handle(WalletChargeCompletedEvent event) {
        try {
            walletHistoryRecoder.record(
                    event.getWalletId(),
                    event.getTransactionType(),
                    event.getAmount(),
                    event.getBalanceAfter(),
                    event.getReferenceType(),
                    event.getReferenceId(),
                    event.getOccurredAt()
            );
        } catch (Exception e) {
            // todo: 이력 보정 수행
        }
    }
}
