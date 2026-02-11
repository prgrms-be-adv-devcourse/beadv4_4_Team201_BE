package app.giftify.support.jpa.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyHistoryRepository extends JpaRepository<IdempotencyHistory, Long> {
}
