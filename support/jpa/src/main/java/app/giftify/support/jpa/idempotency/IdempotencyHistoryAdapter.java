package app.giftify.support.jpa.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdempotencyHistoryAdapter implements IdempotencyHistoryRepository{

    private final JpaIdempotencyHistoryRepository jpaIdempotencyHistoryRepository;

    @Override
    public void save(IdempotencyHistory history) {
        jpaIdempotencyHistoryRepository.save(history);
    }

    @Override
    public Optional<IdempotencyHistory> findByIdempotencyKey(String idempotencyKey) {
        return jpaIdempotencyHistoryRepository.findByIdempotencyKey(idempotencyKey);
    }
}
