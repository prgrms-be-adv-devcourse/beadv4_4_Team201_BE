package app.giftify.orderDemo.domain.errorCode;

import org.springframework.http.HttpStatus;

public enum InfraErrorCode implements ErrorCode {

    DB_LOCK_TIMEOUT(HttpStatus.INTERNAL_SERVER_ERROR, "INFRA_DB_001", "DB 락 획득에 실패했습니다.", true),
    DB_TEMPORARY_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "INFRA_DB_002", "일시적인 DB 오류입니다.", true),
    DB_CONSTRAINT_VIOLATION(HttpStatus.INTERNAL_SERVER_ERROR, "INFRA_DB_003", "DB 제약 조건 위반입니다.", false),

    EXTERNAL_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "INFRA_EXT_001", "외부 API 응답 지연", true),
    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "INFRA_EXT_002", "외부 API 호출 실패", true),

    UNKNOWN_INFRA_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INFRA_999", "알 수 없는 인프라 오류", false);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final boolean retryable;

    InfraErrorCode(HttpStatus status, String code, String message, boolean retryable) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.retryable = retryable;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }
}