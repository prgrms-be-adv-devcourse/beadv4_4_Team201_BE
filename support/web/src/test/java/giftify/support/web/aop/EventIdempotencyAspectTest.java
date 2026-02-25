package giftify.support.web.aop;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.support.common.annotation.EventIdempotent;
import giftify.support.web.idempotency.aop.EventIdempotencyAspect;
import giftify.support.web.idempotency.manager.IdempotencyManager;
import giftify.support.web.idempotency.util.PayloadHasher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventIdempotencyAspectTest {

    @InjectMocks
    private EventIdempotencyAspect aspect;

    @Mock
    private IdempotencyManager idempotencyManager;

    @Mock
    private PayloadHasher payloadHasher;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private EventIdempotent annotation;

    private static final String EVENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String REDIS_KEY = "EVENT_IDEM:TEST:" + EVENT_ID;
    private static final String HASH = "abc123hash";
    private static final long TTL = 60L;

    private BaseDomainEvent createTestEvent() {
        return new BaseDomainEvent() {
            @Override
            public String getEventId() {
                return EVENT_ID;
            }
        };
    }

    @Test
    @DisplayName("최초 이벤트는 비즈니스 로직을 실행하고 COMPLETED로 업데이트한다")
    void execute_FirstEvent_ProcessesAndCompletes() throws Throwable {
        // given
        BaseDomainEvent event = createTestEvent();
        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        when(annotation.prefix()).thenReturn("TEST");
        when(annotation.ttl()).thenReturn(TTL);
        when(payloadHasher.calculateHash(event)).thenReturn(HASH);
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(true);
        when(joinPoint.proceed()).thenReturn(null);

        // when
        Object result = aspect.execute(joinPoint, annotation);

        // then
        assertThat(result).isNull();
        verify(joinPoint).proceed();
        verify(idempotencyManager).updateToCompleted(REDIS_KEY, HASH);
    }

    @Test
    @DisplayName("중복 이벤트는 비즈니스 로직을 실행하지 않고 스킵한다")
    void execute_DuplicateEvent_SkipsProcessing() throws Throwable {
        // given
        BaseDomainEvent event = createTestEvent();
        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        when(annotation.prefix()).thenReturn("TEST");
        when(annotation.ttl()).thenReturn(TTL);
        when(payloadHasher.calculateHash(event)).thenReturn(HASH);
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(false);

        // when
        Object result = aspect.execute(joinPoint, annotation);

        // then
        assertThat(result).isNull();
        verify(joinPoint, never()).proceed();
        verify(idempotencyManager, never()).updateToCompleted(anyString(), anyString());
    }

    @Test
    @DisplayName("비즈니스 로직 예외 시 Redis 키를 삭제하고 예외를 전파한다")
    void execute_BusinessException_RemovesKeyAndPropagates() throws Throwable {
        // given
        BaseDomainEvent event = createTestEvent();
        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        when(annotation.prefix()).thenReturn("TEST");
        when(annotation.ttl()).thenReturn(TTL);
        when(payloadHasher.calculateHash(event)).thenReturn(HASH);
        when(idempotencyManager.attemptLock(REDIS_KEY, HASH, TTL)).thenReturn(true);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Business Error"));

        // when & then
        assertThatThrownBy(() -> aspect.execute(joinPoint, annotation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Business Error");

        verify(idempotencyManager).removeKey(REDIS_KEY);
        verify(idempotencyManager, never()).updateToCompleted(anyString(), anyString());
    }
}
