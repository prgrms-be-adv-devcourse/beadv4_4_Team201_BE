package giftify.support.web.idempotency;

public record IdempotencyValue(
        IdempotencyStatus status,
        String payloadHash
) {
    public static IdempotencyValue processing(String payloadHash) {
        return new IdempotencyValue(
                IdempotencyStatus.PROCESSING,
                payloadHash
        );
    }

    public static IdempotencyValue completed(String payloadHash) {
        return new IdempotencyValue(IdempotencyStatus.COMPLETED, payloadHash);
    }
}
