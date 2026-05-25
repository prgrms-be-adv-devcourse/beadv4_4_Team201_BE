package app.giftify.order.adapter.inbound.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.support.common.api.exception.BusinessException;
import app.giftify.support.common.api.exception.ErrorCode;
import app.giftify.support.common.api.exception.InfraException;
import app.giftify.support.common.api.response.RsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "app.giftify.order")
public class OrderExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(OrderExceptionHandler.class);


    private static final String SERVER_ERROR_MESSAGE = "서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RsData<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.warn("errorCode={}, message={}", errorCode.getCode(), ex.getMessage(), ex);

        RsData<Void> body = RsData.fail(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }

    @ExceptionHandler(InfraException.class)
    public ResponseEntity<RsData<Void>> handleInfraException(InfraException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        log.error("errorCode={}, message={}", errorCode.getCode(), ex.getMessage(), ex);

        RsData<Void> body = RsData.fail(errorCode.getCode(), SERVER_ERROR_MESSAGE);

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(body);
    }
}
