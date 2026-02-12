package app.giftify.shared.api.exception;

public enum IdempotencyErrorCode implements ErrorCode {
    DUPLICATE_REQUEST(409, "IDEM_001", "이미 처리 중이거나 완료된 요청입니다."),

    MISSING_IDEMPOTENCY_KEY(400, "IDEM_002", "멱등키(X-Idempotency-Key)가 누락되었습니다."),
    PAYLOAD_MISMATCH(400, "IDEM_003", "동일한 멱등키에 대해 요청 데이터가 일치하지 않습니다.");
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
}
