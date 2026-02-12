package giftify.support.web.idempotency.manager;

import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyManager {

    private final RedisTemplate<String, Object> redisTemplate;

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

        try {
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, hash, Duration.ofMinutes(ttl));

            if (Boolean.TRUE.equals(success)) {
                log.info("키 선점 성공 - 최초 요청으로 판단됨. Key: {}, Hash: {}, 만료시간: {}분", key, hash, ttl);
                return true;
            } else {
                String existingHash = (String) redisTemplate.opsForValue().get(key);
                log.warn("키 선점 실패 - 중복 요청 감지됨. Key: {}, 기존 Hash: {}, 신규 Hash: {}", key, existingHash, hash);
                return false;
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 실패 (재시도 중...) - Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("인프라 예외 발생 - Key: {}, Error: {}", key, e.getMessage());
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, "멱등성 검증 중 시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public Optional<String> getStoredHash(String key) {
        Object value = redisTemplate.opsForValue().get(key);

        String hash = value != null ? value.toString() : null;

        return Optional.ofNullable(hash);
    }

    /**
     * 비즈니스 로직 실패 시 재시도를 허용하기 위해 키를 삭제합니다.
     */
    public void removeKey(String key) {
        redisTemplate.delete(key);
        log.info("키 삭제 완료 - 예외 발생으로 인한 재시도 허용. Key: {}", key);
    }
}