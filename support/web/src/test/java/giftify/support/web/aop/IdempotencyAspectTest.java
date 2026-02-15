package giftify.support.web.aop;

import app.giftify.shared.api.exception.IdempotencyErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.support.common.annotation.Idempotent;
import giftify.support.web.idempotency.IdempotencyStatus;
import giftify.support.web.idempotency.IdempotencyValue;
import giftify.support.web.idempotency.aop.IdempotencyAspect;
import giftify.support.web.idempotency.manager.IdempotencyManager;
import giftify.support.web.idempotency.util.PayloadHasher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotencyAspectTest {

    @InjectMocks
    private IdempotencyAspect idempotencyAspect;

    @Mock
    private IdempotencyManager idempotencyManager;

    @Mock
    private PayloadHasher payloadHasher;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Idempotent idempotent;

    private static final String REDIS_KEY = "IDEM:TEST_PREFIX:test-key";
    private static final String CURRENT_HASH = "hash123";
    private static final Long TTL = 10L;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // 1. 실제 메서드 정보를 가져오기 위한 준비 (테스트용 메서드 아무거나 지정)
        Method mockMethod = this.getClass().getDeclaredMethod("setUp");

        // 2. MethodSignature Mocking
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(mockMethod);
        when(signature.getParameterTypes()).thenReturn(mockMethod.getParameterTypes());

        // 3. JoinPoint가 위 signature를 반환하도록 설정
        when(joinPoint.getSignature()).thenReturn(signature);

        // 나머지 기존 설정...
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Idempotency-Key", "test-key");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(idempotent.prefix()).thenReturn("TEST_PREFIX");
        when(idempotent.ttl()).thenReturn(10L);
        when(payloadHasher.calculateHash(any())).thenReturn(CURRENT_HASH);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("최초 요청 시 로직을 실행하고 상태를 COMPLETED로 업데이트한다")
    void success_first_request() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, CURRENT_HASH, TTL)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("Success Result");

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isEqualTo("Success Result");
        verify(joinPoint).proceed();
        verify(idempotencyManager).updateToCompleted(eq(REDIS_KEY), any());
    }

    @Test
    @DisplayName("중복 요청(PROCESSING)이고 해시가 일치하면 202 Accepted를 반환한다")
    void fail_duplicated_request_processing() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, CURRENT_HASH, TTL)).thenReturn(false);
        IdempotencyValue storedValue = new IdempotencyValue(IdempotencyStatus.PROCESSING, CURRENT_HASH);
        when(idempotencyManager.getStoredValue(REDIS_KEY)).thenReturn(storedValue);

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(joinPoint, never()).proceed(); // 비즈니스 로직 실행 안됨
    }

    @Test
    @DisplayName("중복 요청이지만 해시가 다르면 PAYLOAD_MISMATCH 예외가 발생한다")
    void fail_payload_mismatch() {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, CURRENT_HASH, TTL)).thenReturn(false);
        IdempotencyValue storedValue = new IdempotencyValue(IdempotencyStatus.COMPLETED, "different_hash");
        when(idempotencyManager.getStoredValue(REDIS_KEY)).thenReturn(storedValue);

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(PolicyException.class)
                .hasFieldOrPropertyWithValue("errorCode", IdempotencyErrorCode.PAYLOAD_MISMATCH);
    }

    @Test
    @DisplayName("로직 실행 중 예외 발생 시 Redis 키를 삭제하고 예외를 던진다")
    void fail_business_exception_removes_key() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, CURRENT_HASH, TTL)).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Business Error"));

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business Error");

        verify(idempotencyManager).removeKey(REDIS_KEY); // 키 삭제 검증
    }

    @Test
    @DisplayName("선점은 실패했으나 조회 시 값이 없으면(TTL만료) 신규 요청으로 진행한다")
    void success_race_condition_ttl_expired() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, CURRENT_HASH, TTL)).thenReturn(false);
        when(idempotencyManager.getStoredValue(REDIS_KEY)).thenReturn(null); // 조회 직전 만료
        when(joinPoint.proceed()).thenReturn("Fallback Success");

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isEqualTo("Fallback Success");
        verify(joinPoint).proceed();
    }
}