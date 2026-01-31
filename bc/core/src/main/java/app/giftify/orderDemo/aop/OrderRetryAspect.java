package app.giftify.orderDemo.aop;

import app.giftify.orderDemo.domain.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OrderRetryAspect {

    @Around("execution(public * app.giftify.orderDemo.application.OrderService*(..))")
    public Object aroundFacade(ProceedingJoinPoint joinPoint) throws Throwable {
        int attempts = 3;
        for (int i = 0; i < attempts; i++) {
            try {
                return joinPoint.proceed();
            } catch (BusinessException e) {
                if (e.isRetryable() && i < attempts - 1) {
                    log.warn("[Order] 재시도 {}회차, message={}", i + 1, e.getMessage(), e);
                    continue; // 재시도
                }
                throw e;
            } catch (Exception e) {
                log.error("[Order] 예상치 못한 예외 발생, message={}", e.getMessage(), e);
                throw e;
            }
        }
        throw new IllegalStateException("재시도 실패");
    }
}