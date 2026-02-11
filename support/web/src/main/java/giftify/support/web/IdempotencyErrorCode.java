package giftify.support.web;

import app.giftify.shared.api.exception.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum IdempotencyErrorCode implements ErrorCode {
    DUPLICATE_REQUEST(HttpStatus.CONFLICT.value(), "IDEM_001", "이미 처리 중이거나 완료된 요청입니다."),

    MISSING_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST.value(), "IDEM_002", "멱등키(X-Idempotency-Key)가 누락되었습니다."),
    PAYLOAD_MISMATCH(HttpStatus.BAD_REQUEST.value(), "IDEM_003", "동일한 멱등키에 대해 요청 데이터가 일치하지 않습니다.");
    ;

    private final int statusCode;
    private final String code;
    private final String message;

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
