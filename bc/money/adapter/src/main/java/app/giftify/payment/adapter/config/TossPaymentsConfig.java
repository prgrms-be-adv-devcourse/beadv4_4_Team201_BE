package app.giftify.payment.adapter.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TossPaymentsConfig {

	@Bean
	@ConfigurationProperties(prefix = "tosspayments")
	public TossPaymentsProperties tossPaymentsProperties() {
		return new TossPaymentsProperties();
	}

	@Bean
	public RestTemplate tossPaymentsRestTemplate(RestTemplateBuilder builder) {
		return builder
			.setConnectTimeout(Duration.ofSeconds(5))
			.setReadTimeout(Duration.ofSeconds(30))
			.build();
	}
}