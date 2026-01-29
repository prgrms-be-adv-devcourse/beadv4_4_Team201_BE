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

/**
 * Toss Payments API 관련 설정.
 */
@Configuration
public class TossPaymentsConfig {

	@Bean
	@ConfigurationProperties(prefix = "tosspayments")
	public TossPaymentsProperties tossPaymentsProperties() {
		return new TossPaymentsProperties();
	}

	@Bean
	public RestClient tossPaymentsRestClient(TossPaymentsProperties properties) {
		String credentials = properties.getSecretKey() + ":";
		String encodedCredentials = Base64.getEncoder()
			.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

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
