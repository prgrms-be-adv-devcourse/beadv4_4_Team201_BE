package app.giftify.wishlist.adapter.in.web.exceptionHandler;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import app.giftify.wishlist.core.domain.exception.WishlistDomainException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class WishlistExceptionHandler {

	@ExceptionHandler(WishlistDomainException.class)
	public ResponseEntity<?> handleWishlistDomainException(WishlistDomainException e) {
		log.error("[Wishlist Domain Exception] Code: {}, Message: {}", e.getErrorCode().getCode(), e.getMessage());
		return ResponseEntity.badRequest()
			.body(Map.of(
				"code", e.getErrorCode().getCode(),
				"message", e.getMessage()
			));
	}
}
