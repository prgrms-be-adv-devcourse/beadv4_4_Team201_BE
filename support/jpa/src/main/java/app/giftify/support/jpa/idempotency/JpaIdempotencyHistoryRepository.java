package app.giftify.support.jpa.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaIdempotencyHistoryRepository extends JpaRepository<IdempotencyHistory, Long> {
    Optional<IdempotencyHistory> findByIdempotencyKey(String idempotencyKey);
}
