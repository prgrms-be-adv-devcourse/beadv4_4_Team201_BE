package app.giftify.event;

import app.giftify.security.common.config.SharedSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
	exclude = {
		SharedSecurityAutoConfiguration.class
	},
	excludeName = {
		"org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
		"org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
		"org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
		"org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration",
		"org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration"
	}
)
class EventExternalizationTestApp {
}
