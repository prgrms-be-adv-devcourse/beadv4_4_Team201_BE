package app.giftify.auth.application;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import app.giftify.auth.adapter.outbound.client.Auth0RevokeClient;
import app.giftify.auth.application.inbound.LogoutUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

	private final JwtDecoder jwtDecoder;
	private final TokenBlacklistService tokenBlacklistService;
	private final Auth0RevokeClient auth0RevokeClient;

	@Override
	public void logout(LogoutCommand command) {
		Jwt jwt = jwtDecoder.decode(command.accessToken());

		Duration ttl = Duration.between(Instant.now(), jwt.getExpiresAt());
		if (ttl.isNegative()) {
			ttl = Duration.ZERO;
		}

		tokenBlacklistService.revokeToken(jwt.getId(), ttl, "logout");

		if (command.refreshToken() != null) {
			try {
				auth0RevokeClient.revokeRefreshToken(command.refreshToken());
			} catch (Exception e) {
				log.warn("[Logout] Auth0 refresh token revoke failed, continuing. reason={}", e.getMessage());
			}
		}
	}
}
