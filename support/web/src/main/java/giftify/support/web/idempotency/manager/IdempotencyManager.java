package giftify.support.web.idempotency.manager;

import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import giftify.support.web.idempotency.IdempotencyStatus;
import giftify.support.web.idempotency.IdempotencyValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyManager {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration DEFAULT_COMPLETED_TTL = Duration.ofHours(24);

    /**
     * Redis의 SETNX 연산을 사용하여 멱등키를 선점합니다.
     * @param key 멱등키 (IDEM:{PREFIX}:{USER_ID}:{UUID})
     * @param hash 페이로드 해시값
     * @param ttl 만료 시간 (분 단위)
     * @return 성공 시 true (최초 요청), 실패 시 false (중복 요청)
     */
    @Retryable(
            retryFor = {RedisConnectionFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public boolean attemptLock(String key, String hash, long ttl) {
        log.info("키 선점 시도 - Key: {}, Hash: {}", key, hash);

        IdempotencyValue initialValue = new IdempotencyValue(IdempotencyStatus.PROCESSING, hash);

        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, initialValue, Duration.ofMinutes(ttl));

            return Boolean.TRUE.equals(success);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패 (재시도 중...) - Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, "멱등성 검증 중 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * PROCESSING → COMPLETED 상태로 전환.
     * - 키가 살아 있으면 남은 TTL 유지
     * - 이미 만료/직전이면 기본 TTL(DEFAULT_COMPLETED_TTL)로 새로 부여
     */
    public void updateToCompleted(String key, IdempotencyValue oldValue) {
        Long remainingTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        IdempotencyValue completedValue = oldValue.completed();

        if (remainingTtl == null) {
            log.warn("TTL 조회 결과가 null 입니다. 기본 TTL로 COMPLETED 상태를 저장합니다. key={}", key);
            redisTemplate.opsForValue().set(key, completedValue, DEFAULT_COMPLETED_TTL);
            return;
        }

        if (remainingTtl > 0) {
            redisTemplate.opsForValue().set(key, completedValue, Duration.ofSeconds(remainingTtl));
            log.info("상태 업데이트 완료 [COMPLETED] - 남은 만료 시간 {}초 유지, key={}", remainingTtl, key);
        } else {
            log.info("키 TTL이 0 이하입니다. 기본 TTL로 COMPLETED 상태를 저장합니다. key={}", key);
            redisTemplate.opsForValue().set(key, completedValue, DEFAULT_COMPLETED_TTL);
        }
    }

    public IdempotencyValue getStoredValue(String key) {
        return (IdempotencyValue) redisTemplate.opsForValue().get(key);
    }

    /**
     * 비즈니스 로직 실패 시 재시도를 허용하기 위해 키를 삭제합니다.
     */
    public void removeKey(String key) {
        redisTemplate.delete(key);
        log.info("키 삭제 완료 - 예외 발생으로 인한 재시도 허용. Key: {}", key);
    }
}