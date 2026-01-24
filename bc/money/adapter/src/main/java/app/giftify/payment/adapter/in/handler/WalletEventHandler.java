package app.giftify.payment.adapter.in.handler;

import org.springframework.stereotype.Component;

import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.PaymentType;
import domain.exception.DuplicateTransactionException;
import domain.exception.EventIgnoreException;
import domain.exception.WalletException;
import domain.exception.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            case PaymentType.POINT_CHARGE -> walletService.charge(
                    event.getUserId(),
                    event.getAmount(),
                    event.getType().name(),
                    event.getSourceType(),
                    event.getPaymentId()
            );
            case PaymentType.FUNDING -> {
                // FUNDING은 '예치금 + PG' 로 결제하는 복합 결제.
                // PaymentSucceededEvent 가 발생한 시점에서 이미 PG 결제 금액은 펀딩 참여에 직접 사용되므로 지갑 충전 불필요.
                // (CHARGE와 달리 PG 금액이 지갑으로 들어가는 게 아님)
                log.info("[Wallet] FUNDING PG 결제 완료 - 지갑 충전 skip. paymentId={}, userId={}, amount={}",
                        event.getPaymentId(), event.getUserId(), event.getAmount());
            }
            default -> throw new EventIgnoreException(
                    new IllegalArgumentException("Unsupported payment type: " + event.getType())
            );
        }
    }
}
