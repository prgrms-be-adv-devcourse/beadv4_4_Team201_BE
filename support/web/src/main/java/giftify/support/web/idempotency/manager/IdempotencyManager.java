package giftify.support.web.idempotency.manager;

import app.giftify.support.common.api.exception.IdempotencyErrorCode;
import app.giftify.support.common.api.exception.InfraErrorCode;
import app.giftify.support.common.api.exception.InfraException;
import giftify.support.web.idempotency.IdempotencyValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyManager {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration DEFAULT_COMPLETED_TTL = Duration.ofHours(24);

    /**
     * Redis의 SETNX 연산을 사용하여 멱등키를 선점합니다.
     * @param key 멱등키 (IDEM:{PREFIX}:{USER_ID}:{UUID})
     * @param payloadHash 페이로드 해시값
     * @param ttl 만료 시간 (분 단위)
     * @return 성공 시 true (최초 요청), 실패 시 false (중복 요청)
     */
    @Retryable(
            retryFor = {RedisConnectionFailureException.class},
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public boolean attemptLock(String key, String payloadHash, long ttl) {
        log.info("키 선점 시도 - Key: {}", key);

        IdempotencyValue initialValue = IdempotencyValue.processing(payloadHash);

        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, initialValue, Duration.ofMinutes(ttl));

            return Boolean.TRUE.equals(success);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패 (재시도 중...) - key={}", key, e);
            throw e;
        } catch (Exception e) {
            throw new InfraException(
                    InfraErrorCode.UNKNOWN_INFRA_ERROR,
                    "멱등성 검증 중 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    e
            );
        }
    }

    /**
     * PROCESSING → COMPLETED 상태로 전환.
     */
    public void updateToCompleted(String key, String currentHash) {
        Long remainingTtl = redisTemplate.getExpire(key);

        IdempotencyValue completedValue = IdempotencyValue.completed(currentHash);

        if (remainingTtl > 0) {
            redisTemplate.opsForValue().set(key, completedValue, Duration.ofSeconds(remainingTtl));
            log.info("상태 업데이트 완료 [COMPLETED] - TTL 유지: {}s, key: {}", remainingTtl, key);
        } else {
            redisTemplate.opsForValue().set(key, completedValue, DEFAULT_COMPLETED_TTL);
            log.info("만료된 키에 기본 TTL 부여 후 COMPLETED 저장. key: {}", key);
        }
    }

    /**
     * 멱등키에 매핑된 값 조회
     * 이 메서드는 호출 시점에 해당 키가 이미 선점되어 있다는 것을 전제로 한다.
     * 값이 없으면 멱등성 상태 불일치로 간주하고 예외를 발생시킨다.
     *
     * @throws InfraException 멱등키가 선점된 상태인데 값이 없을 때
     */
    public IdempotencyValue getStoredValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);

        if (value instanceof IdempotencyValue storedValue) {
            return storedValue;
        }

        throw new InfraException(
                IdempotencyErrorCode.IDEMPOTENCY_STATE_INCONSISTENT,
                String.format("멱등성 상태 불일치: 값이 없거나 잘못된 형식입니다. key=%s", key)
        );
    }

    /**
     * 비즈니스 로직 실패 시 재시도를 허용하기 위해 키를 삭제합니다.
     */
    public void removeKey(String key) {
        Boolean deleted = redisTemplate.delete(key);
        log.info("키 삭제 시도 - key={}, deleted={}", key, deleted);
    }
}