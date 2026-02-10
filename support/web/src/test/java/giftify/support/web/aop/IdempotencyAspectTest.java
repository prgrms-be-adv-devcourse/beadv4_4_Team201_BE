package giftify.support.web.aop;

import app.giftify.shared.api.exception.PolicyException;
import app.giftify.support.common.annotation.Idempotent;
import giftify.support.web.IdempotencyErrorCode;
import giftify.support.web.manager.IdempotencyManager;
import giftify.support.web.util.PayloadHasher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyAspectTest {

    @Mock
    private IdempotencyManager idempotencyManager;
    @Mock private PayloadHasher payloadHasher;
    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private MethodSignature methodSignature;
    @Mock private Idempotent idempotent;

    @InjectMocks
    private IdempotencyAspect idempotencyAspect;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        // MockHttpServletRequest를 생성하여 RequestContextHolder에 주입
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Idempotent 어노테이션 기본 동작 모킹
        when(idempotent.prefix()).thenReturn("TEST");
        when(idempotent.ttl()).thenReturn(60L);
    }

    @Test
    @DisplayName("성공: 멱등키가 있고 첫 요청이면 로직을 실행한다")
    void execute_success_first_request() throws Throwable {
        // given
        request.addHeader("X-Idempotency-Key", "unique-key");

        when(payloadHasher.calculateHash(any())).thenReturn("hashed-value");

        when(idempotencyManager.attemptLock(anyString(), anyString(), anyLong())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("Success Result");

        // MethodSignature 모킹 (getRequestBodyPayload 대응)
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("dummyMethod", Object.class));
        when(joinPoint.getArgs()).thenReturn(new Object[]{ new Object() });

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isEqualTo("Success Result");
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    @DisplayName("실패: 중복된 요청이면 DUPLICATE_REQUEST 예외를 던진다")
    void execute_fail_duplicate_request() throws Throwable {
        // given
        request.addHeader("X-Idempotency-Key", "duplicate-key");

        when(payloadHasher.calculateHash(any())).thenReturn("hashed-value");

        when(idempotencyManager.attemptLock(anyString(), anyString(), anyLong())).thenReturn(false);

        // MethodSignature 모킹
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("dummyMethod", Object.class));
        when(joinPoint.getArgs()).thenReturn(new Object[]{ new Object() });

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(PolicyException.class)
                .hasFieldOrPropertyWithValue("errorCode", IdempotencyErrorCode.DUPLICATE_REQUEST);

        verify(joinPoint, never()).proceed(); // 비즈니스 로직은 실행되지 않아야 함
    }

    @Test
    @DisplayName("실패: 비즈니스 로직 예외 발생 시 Redis 키를 삭제하고 다시 던진다")
    void execute_fail_business_logic_error() throws Throwable {
        // given
        request.addHeader("X-Idempotency-Key", "error-key");

        when(payloadHasher.calculateHash(any())).thenReturn("hashed-value");

        when(idempotencyManager.attemptLock(anyString(), anyString(), anyLong())).thenReturn(true);

        when(joinPoint.proceed()).thenThrow(new RuntimeException("Business Error"));
        when(joinPoint.getArgs()).thenReturn(new Object[]{ new Object() });

        // MethodSignature 모킹
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("dummyMethod", Object.class));

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(RuntimeException.class);

        verify(idempotencyManager).removeKey(anyString()); // 키 삭제가 호출되어야 함
    }

    // @RequestBody 인자 추출 테스트용 더미 메서드
    private void dummyMethod(@RequestBody Object body) {}
}
