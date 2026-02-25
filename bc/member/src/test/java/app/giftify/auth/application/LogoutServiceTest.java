package app.giftify.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import app.giftify.auth.adapter.outbound.client.Auth0RevokeClient;
import app.giftify.auth.application.inbound.LogoutUseCase;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

	@Mock
	private JwtDecoder jwtDecoder;

	@Mock
	private TokenBlacklistService tokenBlacklistService;

	@Mock
	private Auth0RevokeClient auth0RevokeClient;

	@InjectMocks
	private LogoutService logoutService;

	@Test
	@DisplayName("로그아웃 성공 — 토큰 블랙리스트 등록 + Auth0 리프레시 토큰 무효화")
	void logout_WithRefreshToken_BlacklistsAndRevokes() {
		// given
		String accessToken = "valid.jwt.token";
		String refreshToken = "refresh_token_123";
		String jti = "token-id-123";

		Jwt jwt = Jwt.withTokenValue(accessToken)
			.header("alg", "RS256")
			.claim("sub", "auth0|user1")
			.jti(jti)
			.expiresAt(Instant.now().plusSeconds(3600))
			.issuedAt(Instant.now())
			.build();

		given(jwtDecoder.decode(accessToken)).willReturn(jwt);

		// when
		logoutService.logout(new LogoutUseCase.LogoutCommand(accessToken, refreshToken));

		// then
		then(tokenBlacklistService).should().revokeToken(eq(jti), any(Duration.class), eq("logout"));
		then(auth0RevokeClient).should().revokeRefreshToken(refreshToken);
	}

	@Test
	@DisplayName("로그아웃 성공 — refreshToken 없으면 블랙리스트만 등록")
	void logout_WithoutRefreshToken_BlacklistsOnly() {
		// given
		String accessToken = "valid.jwt.token";
		String jti = "token-id-456";

		Jwt jwt = Jwt.withTokenValue(accessToken)
			.header("alg", "RS256")
			.claim("sub", "auth0|user2")
			.jti(jti)
			.expiresAt(Instant.now().plusSeconds(3600))
			.issuedAt(Instant.now())
			.build();

		given(jwtDecoder.decode(accessToken)).willReturn(jwt);

		// when
		logoutService.logout(new LogoutUseCase.LogoutCommand(accessToken, null));

		// then
		then(tokenBlacklistService).should().revokeToken(eq(jti), any(Duration.class), eq("logout"));
		then(auth0RevokeClient).should(never()).revokeRefreshToken(any());
	}

	@Test
	@DisplayName("로그아웃 — 만료된 토큰이면 TTL을 Duration.ZERO로 설정")
	void logout_ExpiredToken_UseZeroTtl() {
		// given
		String accessToken = "expired.jwt.token";
		String jti = "token-id-789";

		Jwt jwt = Jwt.withTokenValue(accessToken)
			.header("alg", "RS256")
			.claim("sub", "auth0|user3")
			.jti(jti)
			.expiresAt(Instant.now().minusSeconds(60))
			.issuedAt(Instant.now().minusSeconds(7200))
			.build();

		given(jwtDecoder.decode(accessToken)).willReturn(jwt);

		// when
		logoutService.logout(new LogoutUseCase.LogoutCommand(accessToken, null));

		// then
		ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
		then(tokenBlacklistService).should().revokeToken(eq(jti), ttlCaptor.capture(), eq("logout"));
		assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ZERO);
	}

	@Test
	@DisplayName("Auth0 revoke 실패해도 블랙리스트는 등록됨")
	void logout_Auth0RevokeFails_BlacklistStillApplied() {
		// given
		String accessToken = "valid.jwt.token";
		String refreshToken = "bad_refresh_token";
		String jti = "token-id-abc";

		Jwt jwt = Jwt.withTokenValue(accessToken)
			.header("alg", "RS256")
			.claim("sub", "auth0|user4")
			.jti(jti)
			.expiresAt(Instant.now().plusSeconds(3600))
			.issuedAt(Instant.now())
			.build();

		given(jwtDecoder.decode(accessToken)).willReturn(jwt);
		willThrow(new RuntimeException("Auth0 API error"))
			.given(auth0RevokeClient).revokeRefreshToken(refreshToken);

		// when — should not throw
		logoutService.logout(new LogoutUseCase.LogoutCommand(accessToken, refreshToken));

		// then
		then(tokenBlacklistService).should().revokeToken(eq(jti), any(Duration.class), eq("logout"));
	}
}
