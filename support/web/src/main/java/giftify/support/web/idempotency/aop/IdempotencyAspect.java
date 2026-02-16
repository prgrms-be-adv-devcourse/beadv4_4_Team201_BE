package giftify.support.web.idempotency.aop;

import app.giftify.shared.api.exception.IdempotencyErrorCode;
import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.api.response.RsData;
import app.giftify.support.common.annotation.Idempotent;
import giftify.support.web.idempotency.IdempotencyValue;
import giftify.support.web.idempotency.manager.IdempotencyManager;
import giftify.support.web.idempotency.util.PayloadHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private static final int HASH_PREFIX_LENGTH = 8;

    @Around("@annotation(idempotent)")
    public Object execute(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String redisKey = createRedisKey(idempotent.prefix());
        String currentHash = generatePayloadHash(joinPoint);

        // 1. 최초 요청 시도 (Lock & Initial State)
        if (idempotencyManager.attemptLock(redisKey, currentHash, idempotent.ttl())) {
            return processFirstRequest(joinPoint, redisKey, currentHash);
        }

        // 2. 중복 요청 처리
        log.warn("중복 요청 감지 - Key: {}", redisKey);
        IdempotencyValue storedValue = idempotencyManager.getStoredValue(redisKey);

        validatePayloadIntegrity(redisKey, storedValue, currentHash);

        return createDuplicateResponse(storedValue);
    }

    /**
     * 최초 요청을 실행하고 결과에 따라 상태를 업데이트하거나 키를 삭제합니다.
     */
    private Object processFirstRequest(ProceedingJoinPoint joinPoint, String redisKey, String currentHash) throws Throwable {
        log.info("최초 요청 처리 시작 - Key: {}", redisKey);
        try {
            Object result = joinPoint.proceed();
            idempotencyManager.updateToCompleted(redisKey, currentHash);
            return result;
        } catch (Exception e) {
            idempotencyManager.removeKey(redisKey);
            throw e;
        }
    }

    /**
     * 데이터 무결성 검증: 동일 키인데 페이로드가 다르면 정책 위반 예외를 던집니다.
     */
    private void validatePayloadIntegrity(String redisKey, IdempotencyValue storedValue, String currentHash) {
        if (!payloadHasher.isMatch(storedValue.payloadHash(), currentHash)) {
            throw new PolicyException(
                    IdempotencyErrorCode.PAYLOAD_MISMATCH,
                    String.format("동일 멱등키에 대해 페이로드가 불일치합니다. Key: %s 기존 Hash: %s, 현재 Hash: %s",
                            redisKey, getHashPrefix(storedValue.payloadHash()), getHashPrefix(currentHash))
            );
        }
    }

    /**
     * 중복 요청에 대한 표준 응답 생성
     * - PROCESSING: 202 Accepted (아직 처리 중)
     * - COMPLETED: 200 OK (이미 처리 완료)
     */
    private Object createDuplicateResponse(IdempotencyValue storedValue) {
        HttpStatus status = switch (storedValue.status()) {
            case PROCESSING -> HttpStatus.ACCEPTED;
            case COMPLETED  -> HttpStatus.OK;
            case null       -> throw new InfraException(IdempotencyErrorCode.IDEMPOTENCY_STATE_INCONSISTENT, "상태값이 비어있습니다.");
        };

        return ResponseEntity
                .status(status)
                .body(RsData.success(storedValue.status().getDescription()));
    }

    private String createRedisKey(String prefix) {
        String idempotencyKey = extractHeader(IDEM_HEADER);
        return String.format("IDEM:%s:%s", prefix, idempotencyKey);
    }

    private String extractHeader(String headerName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, "요청 컨텍스트를 찾을 수 없습니다.");
        }

        String value = attributes.getRequest().getHeader(headerName);
        if (!StringUtils.hasText(value)) {
            throw new PolicyException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        return value;
    }

    private String generatePayloadHash(ProceedingJoinPoint joinPoint) {
        Object payload = findRequestBody(joinPoint);
        return payloadHasher.calculateHash(payload);
    }

    private Object findRequestBody(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                return args[i];
            }
        }
        return null; // 페이로드가 없는 요청일 수 있음
    }

    private String getHashPrefix(String hash) {
        if (hash == null) {
            return null;
        }
        if (hash.length() <= HASH_PREFIX_LENGTH) {
            return hash;
        }
        return hash.substring(0, HASH_PREFIX_LENGTH);
    }
}