package app.giftify.support.jpa.idempotency;

import java.util.Optional;

public interface IdempotencyHistoryRepository {
    void save(IdempotencyHistory history);

    Optional<IdempotencyHistory> findByIdempotencyKey(String idempotencyKey);
}
