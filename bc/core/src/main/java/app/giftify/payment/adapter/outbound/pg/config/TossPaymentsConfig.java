package app.giftify.payment.adapter.outbound.pg.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import app.giftify.payment.adapter.outbound.pg.TossPaymentsApi;
import lombok.extern.slf4j.Slf4j;

/**
 * Toss Payments API 관련 설정.
 */
@Slf4j
@Configuration
public class TossPaymentsConfig {

	@Bean
	@ConfigurationProperties(prefix = "tosspayments")
	public TossPaymentsProperties tossPaymentsProperties() {
		return new TossPaymentsProperties();
	}

	@Bean
	public RestClient tossPaymentsRestClient(TossPaymentsProperties properties) {
		String secretKey = properties.getSecretKey();
		String baseUrl = properties.getApi().getBaseUrl();

		// 디버깅 로그
		log.info("[TossPaymentsConfig] baseUrl: {}", baseUrl);
		log.info("[TossPaymentsConfig] secretKey is null: {}", secretKey == null);
		log.info("[TossPaymentsConfig] secretKey length: {}", secretKey != null ? secretKey.length() : 0);
		log.info("[TossPaymentsConfig] secretKey prefix: {}",
			secretKey != null && secretKey.length() > 10 ? secretKey.substring(0, 10) + "..." : secretKey);

		String credentials = secretKey + ":";
		String encodedCredentials = Base64.getEncoder()
			.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

		log.info("[TossPaymentsConfig] encoded credentials: {}", encodedCredentials);

		return RestClient.builder()
			.baseUrl(properties.getApi().getBaseUrl())
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
			.requestFactory(clientHttpRequestFactory())
			.build();
	}

	private ClientHttpRequestFactory clientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(5));
		factory.setReadTimeout(Duration.ofSeconds(30));

		return factory;
	}

	@Bean
	public TossPaymentsApi tossPaymentsApi(@Qualifier("tossPaymentsRestClient") RestClient tossPaymentsRestClient) {
		RestClientAdapter adapter = RestClientAdapter.create(tossPaymentsRestClient);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

		return factory.createClient(TossPaymentsApi.class);
	}
}
