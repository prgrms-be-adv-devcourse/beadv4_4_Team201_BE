package giftify.support.web.handler;

import app.giftify.shared.api.exception.ErrorCode;
import app.giftify.shared.api.response.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RsData<Void>> handleException(Exception e) {
        log.error("[Global] 알 수 없는 예외 발생", e);

        GlobalErrorCode errorCode = GlobalErrorCode.UNKNOWN_ERROR;

        RsData<Void> body = RsData.fail(errorCode.getMessage(), errorCode.getCode());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    // todo: 추후 확장 가능하면 분리
    enum GlobalErrorCode implements ErrorCode {
        UNKNOWN_ERROR("GLOBAL_001", "알 수 없는 오류가 발생했습니다.")
        ;

        private final String code;
        private final String message;

        GlobalErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
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
}
