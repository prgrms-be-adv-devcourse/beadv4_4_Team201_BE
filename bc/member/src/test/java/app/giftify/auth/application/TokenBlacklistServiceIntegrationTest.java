package app.giftify.auth.application;

import app.giftify.auth.support.config.TokenBlacklistTestConfig;
import app.giftify.support.common.AbstractRedisTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
	classes = TokenBlacklistTestConfig.class,
	webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class TokenBlacklistServiceIntegrationTest extends AbstractRedisTest {

	@Autowired
	private TokenBlacklistService tokenBlacklistService;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@BeforeEach
	void setUp() {
		// 각 테스트 전 Redis 초기화
		redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
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
	@DisplayName("개별 토큰 무효화")
	class RevokeToken {

		@Test
		@DisplayName("개별 토큰 무효화 후 해당 토큰은 거부되어야 한다")
		void shouldRejectRevokedToken() {
			// Given
			String jti = "test-jti-001";
			Jwt jwt = createJwt(jti, "auth0|user1", Instant.now());

			// When
			tokenBlacklistService.revokeToken(jti, Duration.ofMinutes(30), "manual_revoke");

			// Then
			assertThat(tokenBlacklistService.isTokenRevoked(jwt)).isTrue();
		}

		@Test
		@DisplayName("무효화되지 않은 토큰은 허용되어야 한다")
		void shouldAllowValidToken() {
			// Given
			Jwt jwt = createJwt("valid-jti", "auth0|user1", Instant.now());

			// Then
			assertThat(tokenBlacklistService.isTokenRevoked(jwt)).isFalse();
		}
	}

	@Nested
	@DisplayName("사용자 전체 토큰 무효화")
	class RevokeAllUserTokens {

		@Test
		@DisplayName("무효화 이전에 발급된 토큰은 거부되어야 한다")
		void shouldRejectTokensIssuedBeforeRevocation() throws InterruptedException {
			// Given
			String authSub = "auth0|user2";
			Instant issuedAt = Instant.now();
			Jwt jwt = createJwt("jti-before", authSub, issuedAt);

			// When - 약간의 시간 차이를 두고 무효화
			Thread.sleep(10);
			tokenBlacklistService.revokeAllUserTokens(authSub);

			// Then
			assertThat(tokenBlacklistService.isTokenRevoked(jwt)).isTrue();
		}

		@Test
		@DisplayName("무효화 이후에 발급된 토큰은 허용되어야 한다")
		void shouldAllowTokensIssuedAfterRevocation() throws InterruptedException {
			// Given
			String authSub = "auth0|user3";

			// When - 먼저 무효화
			tokenBlacklistService.revokeAllUserTokens(authSub);
			Thread.sleep(10);

			// Then - 무효화 이후 발급된 토큰
			Jwt jwt = createJwt("jti-after", authSub, Instant.now());
			assertThat(tokenBlacklistService.isTokenRevoked(jwt)).isFalse();
		}
	}

	@Nested
	@DisplayName("글로벌 토큰 무효화")
	class RevokeAllTokens {

		@Test
		@DisplayName("글로벌 무효화 이전에 발급된 모든 토큰은 거부되어야 한다")
		void shouldRejectAllTokensIssuedBeforeGlobalRevocation() throws InterruptedException {
			// Given
			Jwt jwt1 = createJwt("jti-g1", "auth0|user1", Instant.now());
			Jwt jwt2 = createJwt("jti-g2", "auth0|user2", Instant.now());

			// When
			Thread.sleep(10);
			tokenBlacklistService.revokeAllTokens();

			// Then
			assertThat(tokenBlacklistService.isTokenRevoked(jwt1)).isTrue();
			assertThat(tokenBlacklistService.isTokenRevoked(jwt2)).isTrue();
		}
	}

	@Nested
	@DisplayName("TTL 검증")
	class TTLVerification {

		@Test
		@DisplayName("개별 토큰 무효화는 지정된 TTL 후 자동 만료되어야 한다")
		void shouldExpireAfterTTL() throws InterruptedException {
			// Given
			String jti = "ttl-test-jti";
			Jwt jwt = createJwt(jti, "auth0|user", Instant.now());

			// When - 1초 TTL로 무효화
			tokenBlacklistService.revokeToken(jti, Duration.ofSeconds(1), "ttl_test");
			assertThat(tokenBlacklistService.isTokenRevoked(jwt)).isTrue();

			// Then - TTL 만료 후
			Thread.sleep(1500);
			assertThat(tokenBlacklistService.isTokenRevoked(jwt)).isFalse();
		}
	}
}
