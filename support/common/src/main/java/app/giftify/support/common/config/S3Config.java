package app.giftify.support.common.config;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

	private final String endpoint;
	private final String region;
	private final String accessKey;
	private final String secretKey;

	public S3Config(
		@Value("${giftify.storage.s3.endpoint}") String endpoint,
		@Value("${giftify.storage.s3.region}") String region,
		@Value("${giftify.storage.s3.access-key}") String accessKey,
		@Value("${giftify.storage.s3.secret-key}") String secretKey
	) {
		if (!StringUtils.hasText(endpoint)) {
			throw new IllegalArgumentException("s3 endpoint must not be blank");
		}
		if (!StringUtils.hasText(region)) {
			throw new IllegalArgumentException("s3 region must not be blank");
		}
		if (!StringUtils.hasText(accessKey)) {
			throw new IllegalArgumentException("s3 access key must not be blank");
		}
		if (!StringUtils.hasText(secretKey)) {
			throw new IllegalArgumentException("s3 secret key must not be blank");
		}
		this.endpoint = endpoint;
		this.region = region;
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	public URI endpointUri() {
		try {
			return new URI(endpoint);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("invalid s3 endpoint: " + endpoint, e);
		}
	}

	public S3Configuration s3Configuration() {
		return S3Configuration.builder()
			.pathStyleAccessEnabled(true)
			.build();
	}

	@Bean(destroyMethod = "close")
	public S3Client s3Client() {
		return S3Client.builder()
			.endpointOverride(endpointUri())
			.region(Region.of(region))
			.credentialsProvider(staticCredentials())
			.serviceConfiguration(s3Configuration())
			.build();
	}

	@Bean(destroyMethod = "close")
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
			.endpointOverride(endpointUri())
			.region(Region.of(region))
			.credentialsProvider(staticCredentials())
			.serviceConfiguration(s3Configuration())
			.build();
	}

	private StaticCredentialsProvider staticCredentials() {
		return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
	}
}
