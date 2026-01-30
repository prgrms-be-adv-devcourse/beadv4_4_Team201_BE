package app.giftify.settlement.adapter.outbound.aop;

import app.giftify.settlement.domain.errorCode.InfraErrorCode;
import app.giftify.settlement.domain.errorCode.SettlementErrorCode;
import app.giftify.settlement.domain.exception.InfraException;
import app.giftify.settlement.domain.exception.PolicyException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
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
            if (isFundingIdTypeUniqueConstraintViolation(dive)) {
                throw new PolicyException(SettlementErrorCode.DUPLICATE_SETTLEMENT_ITEM);
            } else {
                return new InfraException(InfraErrorCode.DB_CONSTRAINT_VIOLATION, e);
            }
        }

        return new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR, e);
    }

    private boolean isFundingIdTypeUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable cause = e.getCause();
        if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
            String constraintName = cve.getConstraintName();
            String sqlState = cve.getSQLState();
            return "23505".equals(sqlState) && "uk_funding_type".equalsIgnoreCase(constraintName);
        }
        return false;
    }
}