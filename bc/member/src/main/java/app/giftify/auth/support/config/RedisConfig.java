package app.giftify.auth.support.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {
	/**
	 * StringRedisTemplate은 Key-Value 모두 String으로 직렬화하는 템플릿
	 * Token Blacklist에 사용하려고 하는데, 단순 문자열 저장이므로 StringRedisTemplate이 적합
	 */
	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(connectionFactory);
		return template;
	}
}
