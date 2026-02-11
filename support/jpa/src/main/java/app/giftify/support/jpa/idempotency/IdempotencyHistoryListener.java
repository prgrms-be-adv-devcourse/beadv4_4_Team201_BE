package app.giftify.support.jpa.idempotency;

import app.giftify.shared.domain.event.IdempotencySuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class IdempotencyHistoryListener {

    private final IdempotencyHistoryRepository repository;

    @Async // 저장 로직이 메인 로직에 지장을 주지 않도록 비동기 처리
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleIdempotencySuccess(IdempotencySuccessEvent event) {
        IdempotencyHistory idempotencyHistory = new IdempotencyHistory(
                event.getIdempotencyKey(),
                event.getPayloadHash(),
                event.getDomainType(),
                event.getRequesterId()
        );

        repository.save(idempotencyHistory);
    }
}