package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.orderDemo.domain.exception.BusinessException;
import app.giftify.orderDemo.domain.exception.InfraException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class OrderControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        log.info("[OrderControllerAdvice] BusinessException 발생, exMessage={}, errorCodeMessage={}", ex.getMessage(), ex.getErrorCode().getMessage());

        // todo : 추후 RsData 적용
        Map<String, Object> body = Map.of(
                "code", ex.getErrorCode().getCode(),
                "message", ex.getErrorCode().getMessage()
        );

        HttpStatus status = ex.isRetryable() ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(InfraException.class)
    public ResponseEntity<Map<String, Object>> handleInfraException(InfraException ex) {
        log.error("[OrderControllerAdvice] InfraException 발생, message={}, message={}", ex.getMessage(), ex.getErrorCode().getMessage());

        Map<String, Object> body = Map.of(
                "code", ex.getErrorCode().getCode(),
                "message", "서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknownException(Exception ex) {
        log.error("[OrderControllerAdvice] 알 수 없는 예외 발생", ex);

        Map<String, Object> body = Map.of(
                "code", "UNKNOWN_ERROR",
                "message", "알 수 없는 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}