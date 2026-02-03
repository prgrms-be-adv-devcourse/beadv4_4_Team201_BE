package app.giftify.orderDemo.domain.errorCode;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
    boolean isRetryable();
}