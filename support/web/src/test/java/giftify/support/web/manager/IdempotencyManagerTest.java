package giftify.support.web.manager;

import app.giftify.shared.api.exception.IdempotencyErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.support.common.AbstractRedisTest;
import app.giftify.support.common.config.RedisConfig;
import giftify.support.web.idempotency.IdempotencyStatus;
import giftify.support.web.idempotency.IdempotencyValue;
import giftify.support.web.idempotency.manager.IdempotencyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataRedisTest
@Import({IdempotencyManager.class, RedisConfig.class})
class IdempotencyManagerTest extends AbstractRedisTest {

    @Autowired
    private IdempotencyManager idempotencyManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String key = "IDEM:ORDER:TEST_KEY";
    private final String payloadHash = "abc123hash";

    @BeforeEach
    void cleanUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    // --- 1. 선점 (attemptLock) 테스트 ---

    @Test
    @DisplayName("최초 요청 시 Redis에 PROCESSING 상태로 키가 생성되고 true를 반환한다")
    void attemptLock_success_first_time() {
        // when
        boolean success = idempotencyManager.attemptLock(key, payloadHash, 10);

        // then
        assertThat(success).isTrue();

        IdempotencyValue stored = (IdempotencyValue) redisTemplate.opsForValue().get(key);
        assertThat(stored).isNotNull();
        assertThat(stored.status()).isEqualTo(IdempotencyStatus.PROCESSING);
        assertThat(stored.payloadHash()).isEqualTo(payloadHash);

        Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("이미 동일한 키가 존재하면 선점에 실패하고 false를 반환한다")
    void attemptLock_fail_if_already_exists() {
        // given
        idempotencyManager.attemptLock(key, payloadHash, 10);

        // when
        boolean secondAttempt = idempotencyManager.attemptLock(key, payloadHash, 10);

        // then
        assertThat(secondAttempt).isFalse();
    }

    // --- 2. 업데이트 (updateToCompleted) 테스트 ---

    @Test
    @DisplayName("기존 키가 살아있을 때 업데이트하면 기존 TTL을 유지하며 COMPLETED로 변경된다")
    void updateToCompleted_maintains_existing_ttl(){
        // given
        idempotencyManager.attemptLock(key, payloadHash, 60); // 60분 설정

        // when
        idempotencyManager.updateToCompleted(key, payloadHash);

        // then
        IdempotencyValue stored = (IdempotencyValue) redisTemplate.opsForValue().get(key);
        assertThat(stored.status()).isEqualTo(IdempotencyStatus.COMPLETED);

        Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
        assertThat(ttl).isGreaterThan(50); // TTL이 유지되고 있는지 확인
    }

    @Test
    @DisplayName("키가 이미 만료된 후 업데이트가 호출되면 기본 TTL(24시간)로 새로 생성한다")
    void updateToCompleted_falls_back_to_default_ttl() {
        // given: 키가 없는 상태

        // when
        idempotencyManager.updateToCompleted(key, payloadHash);

        // then
        IdempotencyValue stored = (IdempotencyValue) redisTemplate.opsForValue().get(key);
        assertThat(stored.status()).isEqualTo(IdempotencyStatus.COMPLETED);

        Long ttl = redisTemplate.getExpire(key, TimeUnit.HOURS);
        assertThat(ttl).isEqualTo(23L); // 약 24시간 (조회 시점에 따라 23이 될 수 있음)
    }

    // --- 3. 조회 (getStoredValue) 테스트 ---

    @Test
    @DisplayName("정상적으로 저장된 값을 조회한다")
    void getStoredValue_success() {
        // given
        idempotencyManager.attemptLock(key, payloadHash, 10);

        // when
        IdempotencyValue result = idempotencyManager.getStoredValue(key);

        // then
        assertThat(result.status()).isEqualTo(IdempotencyStatus.PROCESSING);
        assertThat(result.payloadHash()).isEqualTo(payloadHash);
    }

    @Test
    @DisplayName("조회 시 키가 존재하지 않으면 상태 불일치 예외를 던진다")
    void getStoredValue_fail_when_missing() {
        // when & then
        assertThatThrownBy(() -> idempotencyManager.getStoredValue(key))
                .isInstanceOf(InfraException.class)
                .hasFieldOrPropertyWithValue("errorCode", IdempotencyErrorCode.IDEMPOTENCY_STATE_INCONSISTENT);
    }

    // --- 4. 삭제 (removeKey) 테스트 ---

    @Test
    @DisplayName("키 삭제 호출 시 Redis에서 데이터가 완전히 제거된다")
    void removeKey_removes_data_from_redis() {
        // given
        idempotencyManager.attemptLock(key, payloadHash, 10);

        // when
        idempotencyManager.removeKey(key);

        // then
        Boolean exists = redisTemplate.hasKey(key);
        assertThat(exists).isFalse();
    }
}