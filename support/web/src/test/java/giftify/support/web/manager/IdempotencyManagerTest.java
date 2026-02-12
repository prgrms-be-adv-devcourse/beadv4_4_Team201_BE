package giftify.support.web.manager;

import app.giftify.support.common.AbstractRedisTest;
import app.giftify.support.common.config.RedisConfig;
import giftify.support.web.idempotency.manager.IdempotencyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@Import({IdempotencyManager.class, RedisConfig.class})
class IdempotencyManagerTest extends AbstractRedisTest {

    @Autowired
    private IdempotencyManager idempotencyManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final String testKey = "IDEM:TEST:12345";
    private final String testHash = "abc123hash";

    @BeforeEach
    void cleanUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    @DisplayName("키 선점 테스트 (attemptLock)")
    class AttemptLockTest {

        @Test
        @DisplayName("성공: Redis에 키가 없으면 선점에 성공하고 true를 반환한다")
        void should_return_true_when_key_is_absent() {
            // when
            boolean result = idempotencyManager.attemptLock(testKey, testHash, 10);

            // then
            assertThat(result).isTrue();
            String storedHash = (String) redisTemplate.opsForValue().get(testKey);
            assertThat(storedHash).isEqualTo(testHash);
        }

        @Test
        @DisplayName("실패: 이미 동일한 키가 존재하면 false를 반환한다")
        void should_return_false_when_key_already_exists() {
            // given
            redisTemplate.opsForValue().set(testKey, testHash);

            // when
            boolean result = idempotencyManager.attemptLock(testKey, testHash, 10);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("검증: 저장된 데이터의 TTL이 설정한 시간과 일치해야 한다")
        void should_have_correct_ttl() {
            // given
            long ttlMinutes = 5;
            idempotencyManager.attemptLock(testKey, testHash, ttlMinutes);

            // when
            Long expire = redisTemplate.getExpire(testKey, TimeUnit.MINUTES);

            // then
            assertThat(expire).isNotNull().isGreaterThan(0).isLessThanOrEqualTo(ttlMinutes);
        }
    }

    @Nested
    @DisplayName("해시 조회 및 삭제 테스트")
    class HashAndRemoveTest {

        @Test
        @DisplayName("조회: 키가 존재하면 Optional에 해시값이 담겨야 한다")
        void getStoredHash_should_return_hash_when_present() {
            // given
            redisTemplate.opsForValue().set(testKey, testHash);

            // when
            Optional<String> result = idempotencyManager.getStoredHash(testKey);

            // then
            assertThat(result).isPresent().contains(testHash);
        }

        @Test
        @DisplayName("삭제: removeKey 호출 시 Redis에서 해당 키가 사라져야 한다")
        void removeKey_should_delete_from_redis() {
            // given
            redisTemplate.opsForValue().set(testKey, testHash);

            // when
            idempotencyManager.removeKey(testKey);

            // then
            Boolean exists = redisTemplate.hasKey(testKey);
            assertThat(exists).isFalse();
        }
    }
}