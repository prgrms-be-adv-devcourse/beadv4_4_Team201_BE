package app.giftify.payment.adapter.in.handler;

import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.event.payment.PaymentType;
import domain.exception.DuplicateTransactionException;
import domain.exception.EventIgnoreException;
import domain.exception.WalletException;
import domain.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import wallet.service.WalletService;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventHandler {

    private final WalletService walletService;

    public void handle(PaymentSucceededEvent event) {
        try {
            processPayment(event);
        } catch (DuplicateTransactionException | WalletNotFoundException e) {
            throw new EventIgnoreException(e);
        } catch (WalletException e) {
            // 도메인 예외 → 로그만 찍음
            log.warn("Business domain exception occurred. eventId={}, memberId={}, reason={}",
                    event.getEventId(),
                    event.getUserId(),
                    e.getErrorCode().toString()
            );
        }
        // todo: DB락이나 네트워크 이슈로 실패할 경우 재시도 로직 반영
    }

    private void processPayment(PaymentSucceededEvent event) {
        switch (event.getType()) {
            case PaymentType.CHARGE -> walletService.charge(
                    event.getUserId(), 
                    event.getAmount(), 
                    event.getType().name(), 
                    event.getSourceType(), 
                    event.getPaymentId()
            );
            // todo: case PaymentType.WITHDRAW -> ...
            default -> throw new EventIgnoreException(
                    new IllegalArgumentException("Unsupported payment type: " + event.getType())
            );
        }
    }
}
