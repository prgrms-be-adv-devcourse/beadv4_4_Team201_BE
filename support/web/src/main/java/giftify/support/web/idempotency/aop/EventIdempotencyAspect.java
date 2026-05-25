package giftify.support.web.idempotency.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.support.common.annotation.EventIdempotent;
import giftify.support.web.idempotency.manager.IdempotencyManager;
import giftify.support.web.idempotency.util.PayloadHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class EventIdempotencyAspect {

	private final IdempotencyManager idempotencyManager;
	private final PayloadHasher payloadHasher;

	@Around("@annotation(annotation)")
	public Object execute(ProceedingJoinPoint joinPoint, EventIdempotent annotation) throws Throwable {
		BaseDomainEvent event = extractEvent(joinPoint);
		String redisKey = createRedisKey(annotation.prefix(), event.getEventId());
		String hash = payloadHasher.calculateHash(event);

		// 중복 이벤트 → 스킵
		if (!idempotencyManager.attemptLock(redisKey, hash, annotation.ttl())) {
			log.info("[EventIdempotent] 중복 이벤트 스킵 - key: {}", redisKey);
			return null;
		}

		// 최초 이벤트 → 실행
		try {
			Object result = joinPoint.proceed();
			idempotencyManager.updateToCompleted(redisKey, hash);
			return result;
		} catch (Exception e) {
			idempotencyManager.removeKey(redisKey);  // 실패 시 재시도 허용
			throw e;
		}
	}

	private BaseDomainEvent extractEvent(ProceedingJoinPoint joinPoint) {
		for (Object arg : joinPoint.getArgs()) {
			if (arg instanceof BaseDomainEvent event) {
				return event;
			}
		}
		throw new IllegalArgumentException(
			"@EventIdempotent requires a BaseDomainEvent parameter. Method: "
				+ joinPoint.getSignature().toShortString());
	}

	private String createRedisKey(String prefix, String eventId) {
		return String.format("EVENT_IDEM:%s:%s", prefix, eventId);
	}
}
