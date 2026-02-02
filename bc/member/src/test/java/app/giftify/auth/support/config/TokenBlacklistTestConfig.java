package app.giftify.auth.support.config;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;

import app.giftify.auth.application.TokenBlacklistService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
@Import(RedisAutoConfiguration.class)
public class TokenBlacklistTestConfig {

	@Bean
	public MeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}

	@Bean
	public TokenBlacklistService tokenBlacklistService(
		StringRedisTemplate stringRedisTemplate,
		MeterRegistry meterRegistry
	) {
		return new TokenBlacklistService(stringRedisTemplate, meterRegistry);
	}
}
