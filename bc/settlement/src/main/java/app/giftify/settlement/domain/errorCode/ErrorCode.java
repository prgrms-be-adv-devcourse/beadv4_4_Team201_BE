package app.giftify.settlement.domain.errorCode;

public interface ErrorCode {
    String getCode();
    String getMessage();
    boolean isRetryable();
}