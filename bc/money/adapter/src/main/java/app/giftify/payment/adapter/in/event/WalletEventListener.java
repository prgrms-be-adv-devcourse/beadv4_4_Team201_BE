package app.giftify.payment.adapter.in.event;

import app.giftify.shared.domain.payment.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import wallet.service.WalletService;

@Component
@RequiredArgsConstructor
public class WalletEventListener {

    private final WalletService walletService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(PaymentSucceededEvent event) {
        try {
            walletService.charge(
                    event.getUserId(),
                    event.getAmount(),
                    event.getType().name(),
                    event.getSourceType(),
                    event.getPaymentId()
            );
        } catch (Exception e) {
            /*
             todo: 재시도 or Dead Letter
             */
        }
    }
}
