package giftify.support.web.handler;

import app.giftify.shared.api.exception.ErrorCode;
import app.giftify.shared.api.response.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<RsData<Void>> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[Global] 요청 파라미터 검증 실패: {}", errorMessage);

        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_PARAMETER;

        RsData<Void> body = RsData.fail(errorMessage, errorCode.getCode());

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[Global] 잘못된 인자: {}", e.getMessage());

        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_PARAMETER;

        RsData<Void> body = RsData.fail(e.getMessage(), errorCode.getCode());

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleException(Exception e) {
        log.error("[Global] 알 수 없는 예외 발생", e);

        GlobalErrorCode errorCode = GlobalErrorCode.UNKNOWN_ERROR;

        RsData<Void> body = RsData.fail(errorCode.getMessage(), errorCode.getCode());

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }

    // todo: 추후 확장 가능하면 분리
    enum GlobalErrorCode implements ErrorCode {
        UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "GLOBAL_001", "알 수 없는 오류가 발생했습니다."),
        INVALID_PARAMETER(HttpStatus.BAD_REQUEST.value(), "GLOBAL_002", "잘못된 요청 파라미터입니다.")
        ;

        private final int statusCode;
        private final String code;
        private final String message;

        GlobalErrorCode(int statusCode, String code, String message) {
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
}
