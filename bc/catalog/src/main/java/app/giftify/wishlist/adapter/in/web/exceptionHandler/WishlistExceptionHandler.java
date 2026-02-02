package app.giftify.wishlist.adapter.in.web.exceptionHandler;

import app.giftify.wishlist.core.domain.exception.WishlistDomainException;
import app.giftify.wishlist.core.domain.exception.WishlistErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class WishlistExceptionHandler {

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
                "status", errorCode.getStatus().value(),
                "code", errorCode.getCode(),
                "message", e.getMessage()
        );

        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }
}
