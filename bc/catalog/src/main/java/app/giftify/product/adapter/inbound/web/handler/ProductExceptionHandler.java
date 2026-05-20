package app.giftify.product.adapter.inbound.web.handler;

import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ProductExceptionHandler {

    /**
     * // 응답 예시
     * {
     * "status": 404,
     * "code": "P001",
     * "message": "상품을 찾을 수 없습니다."
     * }
     */
    @ExceptionHandler(ProductException.class)
    public ResponseEntity<Map<String, Object>> handleProductException(ProductException ex) {
        ProductErrorCode errorCode = (ProductErrorCode) ex.getErrorCode();
        String message = ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage(); //todo 통일

        Map<String, Object> body = Map.of(
                "status", errorCode.getStatusCode(),
                "code", errorCode.getCode(),
                "message", message
        );

        return ResponseEntity.status(errorCode.getStatusCode()).body(body);
    }
}
