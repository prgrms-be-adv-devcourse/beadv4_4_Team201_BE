package app.giftify.payment.adapter.in.handler;

import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import domain.exception.DuplicateTransactionException;
import domain.exception.EventIgnoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WalletEventExceptionHandler {

    public void handle(PaymentSucceededEvent event, Exception e) {
        if (e instanceof EventIgnoreException) {
            logIgnore(event, e.getCause());
        } else {
            log.error(
                    "Event handling failed. eventId={}, memberId={}",
                    event.getEventId(),
                    event.getUserId(),
                    e
            );
        }
    }

    private void logIgnore(PaymentSucceededEvent event, Throwable cause) {
        if (cause instanceof DuplicateTransactionException) {
            log.info(
                    "Ignore event (duplicate). eventId={}, txType={}, memberId={}",
                    event.getEventId(),
                    event.getType(),
                    event.getUserId()
            );
        } else {
            log.warn(
                    "Ignore event (business). cause={}, eventId={}, memberId={}",
                    cause.getClass().getSimpleName(),
                    event.getEventId(),
                    event.getUserId(),
                    cause
            );
        }
    }
}