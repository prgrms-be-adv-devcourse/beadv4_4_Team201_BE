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
                    "Event handling failed. eventId={}, memberId={}, reason={}",
                    event.getEventId(),
                    event.getUserId(),
                    e.getMessage(),
                    e
            );
        }
    }

    private void logIgnore(PaymentSucceededEvent event, Throwable cause) {
        if (cause instanceof DuplicateTransactionException) {
            log.info(
                    "Ignore event (duplicate). eventId={}, txType={}, memberId={}, reason={}",
                    event.getEventId(),
                    event.getType(),
                    event.getUserId(),
                    cause.getMessage()
            );
        } else {
            log.warn(
                    "Ignore event (business). cause={}, eventId={}, memberId={}, reason={}",
                    cause.getClass().getSimpleName(),
                    event.getEventId(),
                    event.getUserId(),
                    cause.getMessage(),
                    cause
            );
        }
    }
}