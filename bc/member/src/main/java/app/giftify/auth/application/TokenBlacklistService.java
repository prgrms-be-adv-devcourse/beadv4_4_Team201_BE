package app.giftify.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
@Service
public class TokenBlacklistService {
	private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);


	private final StringRedisTemplate redisTemplate;

	// Metrics
	private final Counter checkAllowedCounter;
	private final Counter checkRevokedIndividualCounter;
	private final Counter checkRevokedUserCounter;
	private final Counter checkRevokedGlobalCounter;
	private final Counter redisFailureCounter;
	private final Counter revokeIndividualCounter;
	private final Counter revokeUserCounter;
	private final Counter revokeGlobalCounter;
	private final Timer checkTimer;

	private static final String BLACKLIST_PREFIX = "token:blacklist:";
	private static final String USER_REVOKED_PREFIX = "token:revoked:user:";
	private static final String GLOBAL_REVOKED_KEY = "token:revoked:global";

	// Access Token 최대 수명 + 버퍼
	private static final Duration USER_REVOKED_TTL = Duration.ofHours(2);

	public TokenBlacklistService(StringRedisTemplate redisTemplate, MeterRegistry meterRegistry) {
		this.redisTemplate = redisTemplate;

		// Token check counters
		this.checkAllowedCounter = Counter.builder("token.blacklist.check")
			.tag("result", "allowed")
			.description("Token checks that were allowed")
			.register(meterRegistry);

		this.checkRevokedIndividualCounter = Counter.builder("token.blacklist.check")
			.tag("result", "revoked")
			.tag("type", "individual")
			.description("Tokens revoked individually")
			.register(meterRegistry);

		this.checkRevokedUserCounter = Counter.builder("token.blacklist.check")
			.tag("result", "revoked")
			.tag("type", "user")
			.description("Tokens revoked by user-level revocation")
			.register(meterRegistry);

		this.checkRevokedGlobalCounter = Counter.builder("token.blacklist.check")
			.tag("result", "revoked")
			.tag("type", "global")
			.description("Tokens revoked by global revocation")
			.register(meterRegistry);

		// Redis failure counter (CRITICAL for alerting)
		this.redisFailureCounter = Counter.builder("token.blacklist.redis.failure")
			.description("Redis failures during token check (fail-open triggered)")
			.register(meterRegistry);

		// Revocation counters
		this.revokeIndividualCounter = Counter.builder("token.blacklist.revoke")
			.tag("type", "individual")
			.description("Individual token revocations")
			.register(meterRegistry);

		this.revokeUserCounter = Counter.builder("token.blacklist.revoke")
			.tag("type", "user")
			.description("User-level token revocations")
			.register(meterRegistry);

		this.revokeGlobalCounter = Counter.builder("token.blacklist.revoke")
			.tag("type", "global")
			.description("Global token revocations")
			.register(meterRegistry);

		// Check duration timer
		this.checkTimer = Timer.builder("token.blacklist.check.duration")
			.description("Time to check token revocation status")
			.register(meterRegistry);
	}

	/**
	 * 토큰이 무효화되었는지 확인
	 */
	public boolean isTokenRevoked(Jwt jwt) {
		return checkTimer.record(() -> doCheckTokenRevoked(jwt));
	}

	private boolean doCheckTokenRevoked(Jwt jwt) {
		try {
			if (isBlacklisted(jwt.getId())) {
				checkRevokedIndividualCounter.increment();
				return true;
			}
			if (isUserRevoked(jwt.getSubject(), jwt.getIssuedAt())) {
				checkRevokedUserCounter.increment();
				return true;
			}
			if (isGloballyRevoked(jwt.getIssuedAt())) {
				checkRevokedGlobalCounter.increment();
				return true;
			}
			checkAllowedCounter.increment();
			return false;
		} catch (Exception e) {
			// Fail-open: Redis 장애 시 토큰 허용
			redisFailureCounter.increment();
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
		try {
			Instant revokedAt = Instant.ofEpochMilli(Long.parseLong(userRevokedTime));
			boolean revoked = issuedAt.isBefore(revokedAt);
			if (revoked) {
				log.debug("Token for user {} was issued before revocation", subject);
			}
			return revoked;
		} catch (NumberFormatException e) {
			log.warn("Invalid revocation time format for user {}: {}", subject, userRevokedTime);
			return false;
		}
	}

	private boolean isGloballyRevoked(Instant issuedAt) {
		if (issuedAt == null) {
			return false;
		}
		String globalRevokedTime = redisTemplate.opsForValue().get(GLOBAL_REVOKED_KEY);
		if (globalRevokedTime == null) {
			return false;
		}
		try {
			Instant revokedAt = Instant.ofEpochMilli(Long.parseLong(globalRevokedTime));
			boolean revoked = issuedAt.isBefore(revokedAt);
			if (revoked) {
				log.debug("Token was issued before global revocation");
			}
			return revoked;
		} catch (NumberFormatException e) {
			log.warn("Invalid global revocation time format: {}", globalRevokedTime);
			return false;
		}
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
		revokeIndividualCounter.increment();
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
		revokeUserCounter.increment();
		log.info("All tokens revoked for user: {}", authSub);
	}

	public void revokeAllTokens() {
		redisTemplate.opsForValue().set(
			GLOBAL_REVOKED_KEY,
			String.valueOf(Instant.now().toEpochMilli())
		);
		revokeGlobalCounter.increment();
		log.warn("GLOBAL TOKEN REVOCATION executed");
	}

}
