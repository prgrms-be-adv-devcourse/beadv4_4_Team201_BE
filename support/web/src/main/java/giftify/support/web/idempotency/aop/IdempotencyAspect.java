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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
        String idempotencyKey = extractIdempotencyKeyOrThrow(getRequest());

        String redisKey = generateRedisKey(idempotent, idempotencyKey);

        String currentHash = generatePayloadHash(joinPoint);

        boolean isFirstRequest = idempotencyManager.attemptLock(redisKey, currentHash, idempotent.ttl());
        IdempotencyValue storedValue = idempotencyManager.getStoredValue(redisKey);

        if (!isFirstRequest) {
            Object duplicateResponse = handleDuplicateRequest(redisKey, storedValue, currentHash);
            if (duplicateResponse != null)
                return duplicateResponse;

            log.info("멱등성 키가 조회 직전 만료되었습니다. 신규 요청으로 취급하여 진행합니다. key={}", redisKey);
        }

        log.info("키 선점 성공 - 최초 요청으로 판단됨. Key: {}, Hash: {}", redisKey, currentHash);

        return proceedWithIdempotency(joinPoint, redisKey, storedValue);
    }

    /**
     * 중복 요청 처리:
     * - 저장된 값이 없으면 null (→ 신규로 처리)
     * - 해시가 다르면 예외
     * - 해시가 같으면 202 Accepted 반환
     */
    private Object handleDuplicateRequest(String redisKey, IdempotencyValue storedValue, String currentHash) {
        if (storedValue == null) return null;

        String storedHash = storedValue.payloadHash();

        // 데이터 무결성 검증 (키는 같은데 페이로드가 다른 경우 차단)
        if (!storedHash.equals(currentHash)) {
            throw new PolicyException(IdempotencyErrorCode.PAYLOAD_MISMATCH);
        }

        log.warn("키 선점 실패 - 중복 요청 감지됨. Key: {}, 기존 Hash: {}, 신규 Hash: {}", redisKey, storedHash, currentHash);

        // 중복 요청에 대한 응답 (현재는 상태 설명만 내려줌)
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(RsData.success(storedValue.status().getDescription()));
    }

    /**
     * 실제 비즈니스 로직 실행:
     * - 성공 시 필요하다면 COMPLETED 업데이트 훅 추가 가능
     * - 실패 시 키 삭제하여 재시도 허용
     */
    private Object proceedWithIdempotency(ProceedingJoinPoint joinPoint, String redisKey, IdempotencyValue storedValue) throws Throwable {
        try {
            Object result = joinPoint.proceed();
             idempotencyManager.updateToCompleted(redisKey, storedValue);
            return result;
        } catch (Exception e) {
            idempotencyManager.removeKey(redisKey);
            throw e;
        }
    }

    private String generatePayloadHash(ProceedingJoinPoint joinPoint) {
        Object payload = getRequestBodyPayload(joinPoint);
        return payloadHasher.calculateHash(payload);
    }

    private static @NonNull String generateRedisKey(Idempotent idempotent, String idempotencyKey) {
        return String.format("IDEM:%s:%s", idempotent.prefix(), idempotencyKey);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, "현재 요청 컨텍스트를 찾을 수 없습니다.");
        }
        return attributes.getRequest();
    }

    private static Object getRequestBodyPayload(ProceedingJoinPoint joinPoint) {
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

    private static String extractIdempotencyKeyOrThrow(HttpServletRequest request) {
        String idempotencyKey = request.getHeader(IDEM_HEADER);
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new PolicyException(IdempotencyErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        return idempotencyKey;
    }
}