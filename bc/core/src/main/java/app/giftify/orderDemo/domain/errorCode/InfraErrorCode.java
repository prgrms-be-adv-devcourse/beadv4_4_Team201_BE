package app.giftify.orderDemo.domain.errorCode;

public enum InfraErrorCode implements ErrorCode {

    DB_LOCK_TIMEOUT("INFRA_DB_001", "DB 락 획득에 실패했습니다.", true),
    DB_TEMPORARY_ERROR("INFRA_DB_002", "일시적인 DB 오류입니다.", true),
    DB_CONSTRAINT_VIOLATION("INFRA_DB_003", "DB 제약 조건 위반입니다.", false),

    EXTERNAL_API_TIMEOUT("INFRA_EXT_001", "외부 API 응답 지연", true),
    EXTERNAL_API_ERROR("INFRA_EXT_002", "외부 API 호출 실패", true),

    UNKNOWN_INFRA_ERROR("INFRA_999", "알 수 없는 인프라 오류", false);

    private final String code;
    private final String message;
    private final boolean retryable;

    InfraErrorCode(String code, String message, boolean retryable) {
        this.code = code;
        this.message = message;
        this.retryable = retryable;
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