package app.giftify.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	private TokenBlacklistService tokenBlacklistService;

	@BeforeEach
	void setUp() {
		tokenBlacklistService = new TokenBlacklistService(redisTemplate);
	}

	private Jwt createJwt(String jti, String subject, Instant issuedAt) {
		return Jwt.withTokenValue("dummy-token")
			.header("alg", "RS256")
			.jti(jti)
			.subject(subject)
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plusSeconds(3600))
			.build();
	}

	@Nested
	@DisplayName("isTokenRevoked 메서드")
	class IsTokenRevoked {

		@Test
		@DisplayName("블랙리스트에 등록된 토큰은 무효화되어야 한다")
		void shouldReturnTrueForBlacklistedToken() {
			// Given
			String jti = "test-jti-123";
			Jwt jwt = createJwt(jti, "auth0|user123", Instant.now());
			given(redisTemplate.hasKey("token:blacklist:" + jti)).willReturn(true);

			// When
			boolean result = tokenBlacklistService.isTokenRevoked(jwt);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("블랙리스트에 없는 토큰은 유효해야 한다")
		void shouldReturnFalseForValidToken() {
			// Given
			String jti = "valid-jti";
			String subject = "auth0|user123";
			Jwt jwt = createJwt(jti, subject, Instant.now());

			given(redisTemplate.hasKey("token:blacklist:" + jti)).willReturn(false);
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("token:revoked:user:" + subject)).willReturn(null);
			given(valueOperations.get("token:revoked:global")).willReturn(null);

			// When
			boolean result = tokenBlacklistService.isTokenRevoked(jwt);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("사용자 전체 토큰 무효화 후 발급된 토큰은 유효해야 한다")
		void shouldAllowTokenIssuedAfterUserRevocation() {
			// Given
			String authSub = "auth0|user123";
			Instant revokedAt = Instant.now().minusSeconds(60);
			Instant issuedAt = Instant.now();  // 무효화 이후 발급

			Jwt jwt = createJwt("jti-456", authSub, issuedAt);
			given(redisTemplate.hasKey(anyString())).willReturn(false);
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("token:revoked:user:" + authSub))
				.willReturn(String.valueOf(revokedAt.toEpochMilli()));
			given(valueOperations.get("token:revoked:global")).willReturn(null);

			// When
			boolean result = tokenBlacklistService.isTokenRevoked(jwt);

			// Then
			assertThat(result).isFalse();
		}

		@Test
		@DisplayName("사용자 전체 토큰 무효화 전에 발급된 토큰은 무효화되어야 한다")
		void shouldRevokeTokenIssuedBeforeUserRevocation() {
			// Given
			String authSub = "auth0|user123";
			Instant issuedAt = Instant.now().minusSeconds(120);
			Instant revokedAt = Instant.now().minusSeconds(60);  // 토큰 발급 후 무효화

			Jwt jwt = createJwt("jti-789", authSub, issuedAt);
			given(redisTemplate.hasKey(anyString())).willReturn(false);
			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get("token:revoked:user:" + authSub))
				.willReturn(String.valueOf(revokedAt.toEpochMilli()));

			// When
			boolean result = tokenBlacklistService.isTokenRevoked(jwt);

			// Then
			assertThat(result).isTrue();
		}

		@Test
		@DisplayName("Redis 장애 시 Fail-open 정책으로 토큰을 허용해야 한다")
		void shouldAllowTokenOnRedisFailure() {
			// Given
			Jwt jwt = createJwt("jti-error", "auth0|user", Instant.now());
			given(redisTemplate.hasKey(anyString()))
				.willThrow(new RuntimeException("Redis connection failed"));

			// When
			boolean result = tokenBlacklistService.isTokenRevoked(jwt);

			// Then
			assertThat(result).isFalse();
		}
	}

	@Nested
	@DisplayName("revokeAllUserTokens 메서드")
	class RevokeAllUserTokens {

		@Test
		@DisplayName("사용자 토큰 무효화 시 TTL이 설정되어야 한다")
		void shouldSetTtlWhenRevokingUserTokens() {
			// Given
			String authSub = "auth0|user123";
			given(redisTemplate.opsForValue()).willReturn(valueOperations);

			// When
			tokenBlacklistService.revokeAllUserTokens(authSub);

			// Then
			verify(valueOperations).set(
				eq("token:revoked:user:" + authSub),
				anyString(),
				eq(Duration.ofHours(2))
			);
		}

		@Test
		@DisplayName("authSub가 null이면 무효화하지 않아야 한다")
		void shouldNotRevokeWhenAuthSubIsNull() {
			// When
			tokenBlacklistService.revokeAllUserTokens(null);

			// Then - Redis와 상호작용이 없어야 함
			verify(redisTemplate, never()).opsForValue();
		}
	}
}
