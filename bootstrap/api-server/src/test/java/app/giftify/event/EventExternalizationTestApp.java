package app.giftify.event;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import app.giftify.security.common.config.SharedSecurityAutoConfiguration;

@SpringBootApplication(
	exclude = {
		SharedSecurityAutoConfiguration.class
	},
	excludeName = {
		"org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
		"org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
		"org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
		"org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration",
		"org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration",
		"org.springframework.modulith.runtime.autoconfigure.SpringModulithRuntimeAutoConfiguration"
	}
)
class EventExternalizationTestApp {
}
