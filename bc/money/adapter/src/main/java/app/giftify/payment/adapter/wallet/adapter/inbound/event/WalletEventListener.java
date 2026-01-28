package app.giftify.payment.adapter.wallet.adapter.inbound.event;

import app.giftify.payment.adapter.in.handler.WalletEventExceptionHandler;
import app.giftify.payment.adapter.in.handler.WalletEventHandler;
import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WalletEventListener {

    private final WalletEventHandler eventHandler;
    private final WalletEventExceptionHandler exceptionHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PaymentSucceededEvent event) {
        try {
            eventHandler.handle(event);
        } catch (Exception e) {
            exceptionHandler.handle(event, e);
        }
    }
}
