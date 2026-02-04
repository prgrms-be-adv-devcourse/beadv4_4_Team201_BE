package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.shared.api.exception.BusinessException;
import app.giftify.shared.api.exception.ErrorCode;
import app.giftify.shared.api.exception.InfraException;
import app.giftify.shared.api.response.RsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "app.giftify.orderDemo")
public class OrderExceptionHandler {

    private static final String SERVER_ERROR_MESSAGE = "서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RsData<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("[OrderExceptionHandler] errorCode={}, message={}", errorCode.getCode(), ex.getMessage(), ex);

        RsData<Void> body = RsData.fail(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }

    @ExceptionHandler(InfraException.class)
    public ResponseEntity<RsData<Void>> handleInfraException(InfraException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.error("[OrderExceptionHandler] errorCode={}, message={}", errorCode.getCode(), ex.getMessage(), ex);

        RsData<Void> body = RsData.fail(errorCode.getCode(), SERVER_ERROR_MESSAGE);

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }
}