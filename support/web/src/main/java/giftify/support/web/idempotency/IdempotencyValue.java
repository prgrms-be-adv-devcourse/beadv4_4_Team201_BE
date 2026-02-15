package giftify.support.web.idempotency;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record IdempotencyValue(
        IdempotencyStatus status,
        String payloadHash
) {
    public IdempotencyValue completed() {
        if (status != IdempotencyStatus.PROCESSING) {
            log.warn("요청이 이미 완료된 상태입니다.");
        }
        return new IdempotencyValue(IdempotencyStatus.COMPLETED, payloadHash);
    }
}
