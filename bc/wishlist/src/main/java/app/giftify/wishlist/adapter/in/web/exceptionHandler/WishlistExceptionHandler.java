package app.giftify.wishlist.adapter.in.web.exceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.giftify.wishlist.core.domain.exception.WishlistDomainException;
import app.giftify.wishlist.core.domain.exception.WishlistErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class WishlistExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(WishlistExceptionHandler.class);


    /**
     * // 응답 예시
     * {
     * "status": 404,
     * "code": "W101",
     * "message": "위시리스트를 찾을 수 없습니다."
     * }
     */
    @ExceptionHandler(WishlistDomainException.class)
    public ResponseEntity<Map<String, Object>> handleWishlistException(WishlistDomainException e) {
        log.error("[Wishlist Domain Exception] Code: {}, Message: {}", e.getErrorCode().getCode(), e.getMessage());
        WishlistErrorCode errorCode = (WishlistErrorCode) e.getErrorCode();

        Map<String, Object> body = Map.of(
                "status", errorCode.getStatusCode(),
                "code", errorCode.getCode(),
                "message", e.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatusCode()).body(body);
    }
}
