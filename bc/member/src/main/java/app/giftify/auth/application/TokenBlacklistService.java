package app.giftify.auth.application;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

	private final StringRedisTemplate redisTemplate;

	private static final String BLACKLIST_PREFIX = "token:blacklist:";
	private static final String USER_REVOKED_PREFIX = "token:revoked:user:";
	private static final String GLOBAL_REVOKED_KEY = "token:revoked:global";

	// Access Token 최대 수명 + 버퍼
	private static final Duration USER_REVOKED_TTL = Duration.ofHours(2);

	/**
	 * 토큰이 무효화되었는지 확인
	 */
	public boolean isTokenRevoked(Jwt jwt) {
		try {
			return isBlacklisted(jwt.getId())
				|| isUserRevoked(jwt.getSubject(), jwt.getIssuedAt())
				|| isGloballyRevoked(jwt.getIssuedAt());
		} catch (Exception e) {
			// Fail-open: Redis 장애 시 토큰 허용
			log.error("Failed to check token revocation, allowing token (fail-open)", e);
			return false;
		}
	}

	private boolean isBlacklisted(String jti) {
		if (jti == null) {
			return false;
		}
		boolean blacklisted = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
		if (blacklisted) {
			log.debug("Token {} is blacklisted", jti);
		}
		return blacklisted;
	}

	private boolean isUserRevoked(String subject, Instant issuedAt) {
		if (subject == null || issuedAt == null) {
			return false;
		}
		String userRevokedTime = redisTemplate.opsForValue().get(USER_REVOKED_PREFIX + subject);
		if (userRevokedTime == null) {
			return false;
		}
		Instant revokedAt = Instant.ofEpochMilli(Long.parseLong(userRevokedTime));
		boolean revoked = issuedAt.isBefore(revokedAt);
		if (revoked) {
			log.debug("Token for user {} was issued before revocation", subject);
		}
		return revoked;
	}

	private boolean isGloballyRevoked(Instant issuedAt) {
		if (issuedAt == null) {
			return false;
		}
		String globalRevokedTime = redisTemplate.opsForValue().get(GLOBAL_REVOKED_KEY);
		if (globalRevokedTime == null) {
			return false;
		}
		Instant revokedAt = Instant.ofEpochMilli(Long.parseLong(globalRevokedTime));
		boolean revoked = issuedAt.isBefore(revokedAt);
		if (revoked) {
			log.debug("Token was issued before global revocation");
		}
		return revoked;
	}

	/**
	 * 개별 토큰 무효화
	 */
	public void revokeToken(String jti, Duration ttl, String reason) {
		if (jti == null) {
			log.warn("Cannot revoke token without jti");
			return;
		}
		redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, reason, ttl);
		log.info("Token {} revoked: {}", jti, reason);
	}

	/**
	 * 특정 사용자의 모든 토큰 무효화 (authSub 기반)
	 */
	public void revokeAllUserTokens(String authSub) {
		if (authSub == null) {
			log.warn("Cannot revoke tokens without authSub");
			return;
		}
		redisTemplate.opsForValue().set(
			USER_REVOKED_PREFIX + authSub,
			String.valueOf(Instant.now().toEpochMilli()),
			USER_REVOKED_TTL
		);
		log.info("All tokens revoked for user: {}", authSub);
	}

	public void revokeAllTokens() {
		redisTemplate.opsForValue().set(
			GLOBAL_REVOKED_KEY,
			String.valueOf(Instant.now().toEpochMilli())
		);
		log.warn("GLOBAL TOKEN REVOCATION executed");
	}

}
