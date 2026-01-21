package app.giftify.auth.support.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 인증 및 JWT 관련 예외 통합 관리
@RestControllerAdvice
public class AuthExceptionHandler {

	// [JWT 검증 실패 예외 처리]
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<?> handleJwtException(JwtException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(Map.of("error", "Invalid Token", "message", e.getMessage()));
	}

	// [인증 실패 예외 처리]
	@ExceptionHandler({AuthenticationException.class, OAuth2AuthenticationException.class})
	public ResponseEntity<?> handleAuthenticationException(Exception e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(Map.of("error", "Authentication Failed", "message", e.getMessage()));
	}

	// [기타 보안 관련 예외 처리]
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleGeneralException(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(Map.of("error", "Internal Server Error", "message", "인증/인가 모듈에서 알 수 없는 오류가 발생했습니다."));
	}
}