package app.giftify.cart.adapter.inbound;

import app.giftify.cart.core.domain.exception.CartErrorCode;
import app.giftify.cart.core.domain.exception.CartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class CartExceptionHandler {
    @ExceptionHandler(CartException.class)
    public ResponseEntity<?> handleFundingException(CartException e) {
        CartErrorCode errorCode = (CartErrorCode) e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(Map.of(
                        "code", errorCode.getCode(),
                        "message", e.getMessage()  // 포맷팅된 메시지 (예: "펀딩을 찾을 수 없습니다. ID: 123")
                ));
    }
}

