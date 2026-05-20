package app.giftify.support.common.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

	private static final String KEY_PREFIX = "giftify:cache:";
	private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

	public Map<String, Duration> ttlPolicies() {
		Map<String, Duration> policies = new LinkedHashMap<>();
		policies.put("products", Duration.ofMinutes(5));
		policies.put("product-detail", Duration.ofMinutes(5));
		policies.put("wishlist", Duration.ofMinutes(1));
		return policies;
	}

	public RedisCacheConfiguration defaultCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(DEFAULT_TTL)
			.prefixCacheNameWith(KEY_PREFIX)
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
			.disableCachingNullValues();
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		Map<String, RedisCacheConfiguration> perCache = new LinkedHashMap<>();
		RedisCacheConfiguration base = defaultCacheConfiguration();
		ttlPolicies().forEach((name, ttl) -> perCache.put(name, base.entryTtl(ttl)));

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(base)
			.withInitialCacheConfigurations(perCache)
			.build();
	}
}
