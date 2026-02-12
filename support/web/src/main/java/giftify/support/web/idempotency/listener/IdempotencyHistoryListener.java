package giftify.support.web.idempotency.listener;

import app.giftify.shared.domain.event.IdempotencySuccessEvent;
import app.giftify.support.jpa.idempotency.IdempotencyHistory;
import app.giftify.support.jpa.idempotency.IdempotencyHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyHistoryListener {

    private final IdempotencyHistoryRepository repository;

    @Async // 이제 비동기도 잘 작동할 겁니다.
    @EventListener
    public void handleIdempotencySuccess(IdempotencySuccessEvent event) {
        log.debug("IdempotencySuccessEvent 수신 완료 eventId = {}", event.getEventId());

        IdempotencyHistory idempotencyHistory = new IdempotencyHistory(
                event.getIdempotencyKey(),
                event.getPayloadHash(),
                event.getDomainType(),
                event.getRequesterId()
        );

        repository.save(idempotencyHistory);
    }


}