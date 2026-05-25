package app.giftify.settlement.adapter.outbound.aop;

import app.giftify.support.common.api.exception.InfraErrorCode;
import app.giftify.support.common.api.exception.InfraException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryExceptionTranslator {

    @Around("this(org.springframework.data.repository.Repository)")
    public Object translate(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataAccessException e) {
            throw mapToInfraException(e);
        }
    }

    private InfraException mapToInfraException(DataAccessException e) {

        if (e instanceof ConcurrencyFailureException) {
            return new InfraException(InfraErrorCode.DB_LOCK_TIMEOUT, e);
        }

        if (e instanceof QueryTimeoutException || e instanceof TransientDataAccessResourceException) {
            return new InfraException(InfraErrorCode.DB_TEMPORARY_ERROR, e);
        }

        if (e instanceof DataIntegrityViolationException dive) {
            return new InfraException(InfraErrorCode.DB_CONSTRAINT_VIOLATION, e);
        }

        return new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, e);
    }
}