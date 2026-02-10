package giftify.support.web.aop;

import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.support.common.annotation.Idempotent;
import giftify.support.web.IdempotencyErrorCode;
import giftify.support.web.manager.IdempotencyManager;
import giftify.support.web.util.PayloadHasher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyManager idempotencyManager;
    private final PayloadHasher payloadHasher;

    private static final String IDEM_HEADER = "X-Idempotency-Key";

    @Around("@annotation(idempotent)")
    public Object execute(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = getRequest();

        String idempotencyKey = request.getHeader(IDEM_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("필수 헤더 누락: {}", IDEM_HEADER);
            throw new PolicyException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);
        }

        String redisKey = String.format("IDEM:%s:%s", idempotent.prefix(), idempotencyKey);

        Object payload = getRequestBodyPayload(joinPoint);
        String currentHash = payloadHasher.calculateHash(payload);

        boolean isFirstRequest = idempotencyManager.attemptLock(redisKey, currentHash, idempotent.ttl());

        if (!isFirstRequest) {
            throw new PolicyException(IdempotencyErrorCode.DUPLICATE_REQUEST);
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            idempotencyManager.removeKey(redisKey);
            throw e;
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, "현재 요청 컨텍스트를 찾을 수 없습니다.");
        }
        return attributes.getRequest();
    }

    private Object getRequestBodyPayload(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                return args[i];
            }
        }
        return null;
    }
}