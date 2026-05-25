package app.giftify.support.common.api.exception;

// todo: 추후 재시도 여부(isRetryable) 필드 추가
public interface ErrorCode {
    int getStatusCode();
    String getCode();
    String getMessage();
    String formatMessage(Object... args);
}