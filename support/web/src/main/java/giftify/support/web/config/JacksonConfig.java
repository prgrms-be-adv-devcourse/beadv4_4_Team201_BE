package giftify.support.web.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Configuration
public class JacksonConfig {

	@Bean
	JsonMapperBuilderCustomizer disableIsGetterVisibility() {
		return builder -> builder.changeDefaultVisibility(
			vc -> vc.withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
		);
	}
}
