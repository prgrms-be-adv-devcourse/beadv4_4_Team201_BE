package app.giftify.orderDemo.domain.errorCode;

public interface ErrorCode {
    String getCode();
    String getMessage();
    boolean isRetryable();
}