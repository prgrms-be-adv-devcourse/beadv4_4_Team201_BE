package app.giftify.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private JwtDecoder jwtDecoder;

	@Mock
	private JwtDecoder idTokenDecoder;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(eventPublisher, jwtDecoder, idTokenDecoder);
	}

	@Nested
	@DisplayName("validateToken 메서드")
	class ValidateTokenTests {

		@Test
		@DisplayName("유효한 토큰이면 true 반환")
		void validateToken_ValidToken_ReturnsTrue() {
			// given
			String validToken = "valid.jwt.token";
			Jwt jwt = createMockJwt("sub123");
			given(jwtDecoder.decode(validToken)).willReturn(jwt);

			// when
			boolean result = authService.validateToken(validToken);

			// then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("null 토큰이면 false 반환")
		void validateToken_NullToken_ReturnsFalse() {
			// when
			boolean result = authService.validateToken(null);

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("빈 토큰이면 false 반환")
		void validateToken_BlankToken_ReturnsFalse() {
			// when
			boolean result = authService.validateToken("   ");

			// then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("JWT 디코딩 실패 시 false 반환")
		void validateToken_JwtException_ReturnsFalse() {
			// given
			String invalidToken = "invalid.jwt.token";
			given(jwtDecoder.decode(invalidToken)).willThrow(new JwtException("Invalid token"));

			// when
			boolean result = authService.validateToken(invalidToken);

			// then
			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("decodeAndValidateToken 메서드 (id_token 검증)")
	class DecodeAndValidateTokenTests {

		@Test
		@DisplayName("유효한 id_token이면 Jwt 반환")
		void decodeAndValidateToken_ValidIdToken_ReturnsJwt() {
			// given
			String idToken = "valid.id.token";
			Jwt expectedJwt = createMockJwt("auth0|12345");
			given(idTokenDecoder.decode(idToken)).willReturn(expectedJwt);

			// when
			Jwt result = authService.decodeAndValidateToken(idToken);

			// then
			assertThat(result).isEqualTo(expectedJwt);
			assertThat(result.getSubject()).isEqualTo("auth0|12345");
		}

		@Test
		@DisplayName("유효하지 않은 id_token이면 예외 발생")
		void decodeAndValidateToken_InvalidIdToken_ThrowsException() {
			// given
			String invalidToken = "invalid.id.token";
			given(idTokenDecoder.decode(invalidToken)).willThrow(new JwtException("Token validation failed"));

			// when & then
			assertThatThrownBy(() -> authService.decodeAndValidateToken(invalidToken))
				.isInstanceOf(OAuth2AuthenticationException.class);
		}
	}

	@Nested
	@DisplayName("decodeAndValidateAccessToken 메서드 (access_token 검증)")
	class DecodeAndValidateAccessTokenTests {

		@Test
		@DisplayName("유효한 access_token이면 Jwt 반환")
		void decodeAndValidateAccessToken_ValidAccessToken_ReturnsJwt() {
			// given
			String accessToken = "valid.access.token";
			Jwt expectedJwt = createMockJwt("api-user");
			given(jwtDecoder.decode(accessToken)).willReturn(expectedJwt);

			// when
			Jwt result = authService.decodeAndValidateAccessToken(accessToken);

			// then
			assertThat(result).isEqualTo(expectedJwt);
		}

		@Test
		@DisplayName("유효하지 않은 access_token이면 예외 발생")
		void decodeAndValidateAccessToken_InvalidAccessToken_ThrowsException() {
			// given
			String invalidToken = "invalid.access.token";
			given(jwtDecoder.decode(invalidToken)).willThrow(new JwtException("Token validation failed"));

			// when & then
			assertThatThrownBy(() -> authService.decodeAndValidateAccessToken(invalidToken))
				.isInstanceOf(OAuth2AuthenticationException.class);
		}
	}

	private Jwt createMockJwt(String subject) {
		return new Jwt(
			"token-value",
			Instant.now(),
			Instant.now().plusSeconds(3600),
			Map.of("alg", "RS256"),
			Map.of("sub", subject)
		);
	}
}
