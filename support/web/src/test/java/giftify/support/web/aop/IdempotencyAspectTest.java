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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    private static final String IDEM_KEY = "test-uuid";
    private static final String REDIS_KEY = "IDEM:TEST:test-uuid";
    private static final String HASH = "hash123456789";
    private static final long TTL = 10L;

    @BeforeEach
    void setUp() {
        // HTTP 헤더 Mocking
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Idempotency-Key", IDEM_KEY);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Annotation 및 Hash Mocking
        lenient().when(idempotent.prefix()).thenReturn("TEST");
        lenient().when(idempotent.ttl()).thenReturn(TTL);
        lenient().when(payloadHasher.calculateHash(any())).thenReturn(HASH);

        // Reflection-based RequestBody find logic을 위한 Mocking (최소화)
        MethodSignature signature = mock(MethodSignature.class);
        Method mockMethod = ReflectionUtils.findMethod(this.getClass(), "setUp");
        lenient().when(signature.getMethod()).thenReturn(mockMethod);
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("최초 요청 시 비즈니스 로직을 실행하고 완료 상태로 업데이트한다")
    void execute_Success_FirstRequest() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("RESULT");

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isEqualTo("RESULT");
        verify(idempotencyManager).updateToCompleted(REDIS_KEY, HASH);
    }

    @Test
    @DisplayName("처리 중인 중복 요청이면 202 Accepted를 반환한다")
    void execute_Duplicate_Processing() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(false);
        when(idempotencyManager.getStoredValue(REDIS_KEY))
                .thenReturn(new IdempotencyValue(IdempotencyStatus.PROCESSING, HASH));
        when(payloadHasher.isMatch(HASH, HASH)).thenReturn(true);

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isInstanceOf(ResponseEntity.class);
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(joinPoint, never()).proceed();
    }

    @Test
    @DisplayName("완료된 중복 요청이면 200 OK를 반환한다")
    void execute_Duplicate_Completed() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(false);
        when(idempotencyManager.getStoredValue(REDIS_KEY))
                .thenReturn(new IdempotencyValue(IdempotencyStatus.COMPLETED, HASH));
        when(payloadHasher.isMatch(HASH, HASH)).thenReturn(true);

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        ResponseEntity<?> response = (ResponseEntity<?>) result;
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("키는 같으나 해시가 다르면 PAYLOAD_MISMATCH 예외가 발생한다")
    void execute_PayloadMismatch() {
        // given
        String differentHash = "differentHash";
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(false);
        when(idempotencyManager.getStoredValue(REDIS_KEY))
                .thenReturn(new IdempotencyValue(IdempotencyStatus.COMPLETED, differentHash));
        when(payloadHasher.isMatch(differentHash, HASH)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(PolicyException.class)
                .hasFieldOrPropertyWithValue("errorCode", IdempotencyErrorCode.PAYLOAD_MISMATCH);
    }

    @Test
    @DisplayName("비즈니스 로직 실행 중 예외가 발생하면 Redis 키를 삭제하고 예외를 전파한다")
    void execute_BusinessException_RemovesKey() throws Throwable {
        // given
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Business Logic Error"));

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business Logic Error");

        verify(idempotencyManager).removeKey(REDIS_KEY);
    }
}