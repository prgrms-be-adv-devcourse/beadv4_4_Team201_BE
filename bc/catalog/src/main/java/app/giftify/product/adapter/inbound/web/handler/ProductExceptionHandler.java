package app.giftify.product.adapter.inbound.web.handler;

import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
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

        Map<String, Object> body = Map.of(
                "status", errorCode.getStatus().value(),
                "code", errorCode.getCode(),
                "message", errorCode.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }
}
