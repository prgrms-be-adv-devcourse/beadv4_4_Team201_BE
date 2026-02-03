package app.giftify.shared.api.exception;

// todo: 추후 재시도 여부(isRetryable) 필드 추가
public interface ErrorCode {
    String getCode();
    String getMessage();
}
