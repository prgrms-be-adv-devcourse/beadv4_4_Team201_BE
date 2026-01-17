package app.giftify.member.adapter.in.web.exceptionHandler;

import app.giftify.member.adapter.in.web.controller.MemberController;
import app.giftify.member.core.domain.exception.MemberDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(assignableTypes = MemberController.class)
public class MemberExceptionHandler {

    @ExceptionHandler(MemberDomainException.class)
    public ResponseEntity<?> handleMemberDomainException(MemberDomainException e) {
        log.error("[Member Module Exception] Code: {}, Message: {}", e.getErrorCode().getCode(), e.getMessage());

        return ResponseEntity
                .status(400) // [비즈니스 예외] 기본적으로 400 Bad Request
                .body(Map.of(
                        "code", e.getErrorCode().getCode(),
                        "message", e.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("[Member Module Validation Error] {}", errorMessage);

        return ResponseEntity
                .status(400)
                .body(Map.of(
                        "code", "VALIDATION_ERROR",
                        "message", errorMessage
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("[Member Module Unexpected Error] ", e);

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "code", "COMMON_ERROR",
                        "message", "멤버 모듈 내부에서 알 수 없는 오류가 발생했습니다."
                ));
    }
}
