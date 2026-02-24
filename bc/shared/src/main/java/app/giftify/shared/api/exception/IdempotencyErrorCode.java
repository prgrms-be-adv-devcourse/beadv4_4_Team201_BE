package app.giftify.shared.api.exception;

public enum IdempotencyErrorCode implements ErrorCode {
    MISSING_IDEMPOTENCY_KEY(400, "IDEM_002", "멱등키(X-Idempotency-Key)가 누락되었습니다."),
    PAYLOAD_MISMATCH(400, "IDEM_003", "동일한 멱등키에 대해 요청 데이터가 일치하지 않습니다."),
    IDEMPOTENCY_STATE_INCONSISTENT(500, "IDEM_004", "멱등성 상태가 일관되지 않습니다. 잠시 후 다시 시도해주세요."),
    ;

    private final int statusCode;
    private final String code;
    private final String message;

    IdempotencyErrorCode(int statusCode, String code, String message) {
        this.statusCode = statusCode;
        this.code = code;
        this.message = message;
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
}
