package giftify.support.web.aop;

import app.giftify.security.common.util.SecurityUtil;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.IdempotencySuccessEvent;
import app.giftify.support.common.annotation.Idempotent;
import giftify.support.web.manager.IdempotencyManager;
import giftify.support.web.util.PayloadHasher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyAspectTest {

    @Mock private IdempotencyManager idempotencyManager;
    @Mock private PayloadHasher payloadHasher;
    @Mock private EventPublisher eventPublisher; // 추가
    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private MethodSignature methodSignature;
    @Mock private Idempotent idempotent;

    @InjectMocks
    private IdempotencyAspect idempotencyAspect;

    private MockHttpServletRequest request;
    private MockedStatic<SecurityUtil> securityUtilMock; // 정적 메서드 모킹용

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 정적 메서드 모킹 시작
        securityUtilMock = mockStatic(SecurityUtil.class);

        when(idempotent.prefix()).thenReturn("TEST");
        when(idempotent.ttl()).thenReturn(60L);
    }

    @AfterEach
    void tearDown() {
        // 정적 메서드 모킹 종료 (필수)
        securityUtilMock.close();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("성공: 첫 요청 성공 시 이벤트를 발행한다 (회원 요청)")
    void execute_success_and_publish_event_member() throws Throwable {
        // given
        String key = "unique-key";
        String hash = "hashed-value";
        Long memberId = 1L;

        request.addHeader("X-Idempotency-Key", key);
        securityUtilMock.when(SecurityUtil::getCurrentMemberId).thenReturn(Optional.of(memberId));

        when(payloadHasher.calculateHash(any())).thenReturn(hash);
        when(idempotencyManager.attemptLock(anyString(), eq(hash), anyLong())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("Success Result");

        // Reflection-based Mocking
        mockMethodSignature();

        // when
        Object result = idempotencyAspect.execute(joinPoint, idempotent);

        // then
        assertThat(result).isEqualTo("Success Result");

        // 이벤트 발행 검증
        verify(eventPublisher, times(1)).publish(argThat(event -> {
                    IdempotencySuccessEvent successEvent = (IdempotencySuccessEvent) event;
                    return successEvent.getIdempotencyKey().equals(key) &&
                            successEvent.getPayloadHash().equals(hash) &&
                            successEvent.getDomainType().equals("TEST") &&
                            successEvent.getRequesterId().equals(memberId);
                }
        ));
    }

    @Test
    @DisplayName("성공: 비회원 요청 시 requesterId가 null인 이벤트를 발행한다")
    void execute_success_and_publish_event_guest() throws Throwable {
        // given
        String key = "guest-key";
        request.addHeader("X-Idempotency-Key", key);

        // SecurityUtil이 빈 값을 반환하도록 설정
        securityUtilMock.when(SecurityUtil::getCurrentMemberId).thenReturn(Optional.empty());

        when(payloadHasher.calculateHash(any())).thenReturn("hash");
        when(idempotencyManager.attemptLock(anyString(), anyString(), anyLong())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("Success");
        mockMethodSignature();

        // when
        idempotencyAspect.execute(joinPoint, idempotent);

        // then
        verify(eventPublisher).publish(argThat(event -> {
            IdempotencySuccessEvent successEvent = (IdempotencySuccessEvent) event;
            return successEvent.getRequesterId() == null;
        }));
    }

    @Test
    @DisplayName("실패: 중복 요청 시에는 이벤트를 발행하지 않는다")
    void execute_fail_no_event_on_duplicate() throws Throwable {
        // given
        request.addHeader("X-Idempotency-Key", "dup-key");
        when(payloadHasher.calculateHash(any())).thenReturn("hash");
        when(idempotencyManager.attemptLock(anyString(), anyString(), anyLong())).thenReturn(false);
        mockMethodSignature();

        // when & then
        assertThatThrownBy(() -> idempotencyAspect.execute(joinPoint, idempotent))
                .isInstanceOf(PolicyException.class);

        // 중복 실패 시 이벤트는 발행되면 안 됨
        verify(eventPublisher, never()).publish(any());
    }

    // 반복되는 MethodSignature 모킹을 위한 헬퍼 메서드
    private void mockMethodSignature() throws NoSuchMethodException {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("dummyMethod", Object.class));
        when(joinPoint.getArgs()).thenReturn(new Object[]{ new Object() });
    }

    private void dummyMethod(@RequestBody Object body) {}
}
