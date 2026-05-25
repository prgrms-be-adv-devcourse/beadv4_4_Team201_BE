package app.giftify.support.common.api.exception;

public enum InfraErrorCode implements ErrorCode {

    DB_LOCK_TIMEOUT(500, "INFRA_DB_001", "DB 락 획득에 실패했습니다.", true),
    DB_TEMPORARY_ERROR(503, "INFRA_DB_002", "일시적인 DB 오류입니다.", true),
    DB_CONSTRAINT_VIOLATION(500, "INFRA_DB_003", "DB 제약 조건 위반입니다.", false),

    EXTERNAL_API_TIMEOUT(504, "INFRA_EXT_001", "외부 API 응답 지연", true),
    EXTERNAL_API_ERROR(502, "INFRA_EXT_002", "외부 API 호출 실패", true),

    UNKNOWN_INFRA_ERROR(500, "INFRA_999", "알 수 없는 인프라 오류", false);

    private final int statusCode;
    private final String code;
    private final String message;
    private final boolean retryable;

    InfraErrorCode(int statusCode, String code, String message, boolean retryable) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
        this.retryable = retryable;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
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
    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }

    public boolean isRetryable() {
        return retryable;
    }
}