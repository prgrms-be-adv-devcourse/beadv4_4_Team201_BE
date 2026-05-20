package app.giftify.support.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

class CacheConfigTest {

	private CacheConfig config;
	private RedisCacheManager cacheManager;

	@BeforeEach
	void setUp() {
		config = new CacheConfig();
		RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
		cacheManager = config.cacheManager(connectionFactory);
		cacheManager.initializeCaches();
	}

	@Nested
	@DisplayName("도메인별 캐시 사전 등록")
	class PreconfiguredCaches {

		@Test
		@DisplayName("products, product-detail, wishlist, jwks 캐시가 사전 등록된다")
		void domain_caches_are_preconfigured() {
			Set<String> names = (Set<String>) cacheManager.getCacheNames();
			assertThat(names).contains("products", "product-detail", "wishlist", "jwks");
		}
	}

	@Nested
	@DisplayName("도메인별 TTL 정책")
	class DomainTtl {

		@Test
		@DisplayName("products 캐시 TTL 은 5분")
		void products_ttl_is_5_minutes() {
			assertThat(ttlOf("products")).isEqualTo(Duration.ofMinutes(5));
		}

		@Test
		@DisplayName("product-detail 캐시 TTL 은 5분")
		void product_detail_ttl_is_5_minutes() {
			assertThat(ttlOf("product-detail")).isEqualTo(Duration.ofMinutes(5));
		}

		@Test
		@DisplayName("wishlist 캐시 TTL 은 1분")
		void wishlist_ttl_is_1_minute() {
			assertThat(ttlOf("wishlist")).isEqualTo(Duration.ofMinutes(1));
		}

		@Test
		@DisplayName("jwks 캐시 TTL 은 5분")
		void jwks_ttl_is_5_minutes() {
			assertThat(ttlOf("jwks")).isEqualTo(Duration.ofMinutes(5));
		}
	}

	@Nested
	@DisplayName("캐시 키 prefix 정책")
	class KeyPrefix {

		@Test
		@DisplayName("모든 캐시 키는 giftify:cache: prefix 로 시작한다")
		void prefix_starts_with_giftify_cache() {
			RedisCache productsCache = (RedisCache) cacheManager.getCache("products");
			assertThat(productsCache).isNotNull();
			String prefix = productsCache.getCacheConfiguration().getKeyPrefixFor("products");
			assertThat(prefix).startsWith("giftify:cache:");
		}
	}

	@Nested
	@DisplayName("기본 캐시 설정 fallback")
	class DefaultCacheConfig {

		@Test
		@DisplayName("사전 등록 외 캐시 이름은 기본 TTL 10분 적용")
		void unregistered_cache_uses_default_ttl() {
			RedisCacheConfiguration defaults = config.defaultCacheConfiguration();
			assertThat(defaults.getTtlFunction().getTimeToLive(new Object(), new Object()))
				.isEqualTo(Duration.ofMinutes(10));
		}
	}

	@Nested
	@DisplayName("도메인별 TTL 맵 노출")
	class TtlMap {

		@Test
		@DisplayName("ttlPolicies() 가 4개 도메인 매핑을 반환한다")
		void ttl_policies_map_has_four_entries() {
			Map<String, Duration> policies = config.ttlPolicies();
			assertThat(policies).hasSize(4)
				.containsEntry("products", Duration.ofMinutes(5))
				.containsEntry("product-detail", Duration.ofMinutes(5))
				.containsEntry("wishlist", Duration.ofMinutes(1))
				.containsEntry("jwks", Duration.ofMinutes(5));
		}
	}

	private Duration ttlOf(String cacheName) {
		RedisCache cache = (RedisCache) cacheManager.getCache(cacheName);
		assertThat(cache).isNotNull();
		return cache.getCacheConfiguration().getTtlFunction()
			.getTimeToLive(new Object(), new Object());
	}
}
