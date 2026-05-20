package app.giftify.support.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class S3ConfigTest {

	private static final String VALID_ENDPOINT = "http://minio:9000";
	private static final String VALID_REGION = "us-east-1";
	private static final String VALID_ACCESS_KEY = "minioadmin";
	private static final String VALID_SECRET_KEY = "minioadmin";

	private S3Config config;

	@BeforeEach
	void setUp() {
		config = new S3Config(VALID_ENDPOINT, VALID_REGION, VALID_ACCESS_KEY, VALID_SECRET_KEY);
	}

	@Nested
	@DisplayName("Bean 등록")
	class BeanWiring {

		@Test
		@DisplayName("S3Client 빈이 정상 생성된다")
		void s3client_bean_is_created() {
			S3Client client = config.s3Client();
			assertThat(client).isNotNull();
			client.close();
		}

		@Test
		@DisplayName("S3Presigner 빈이 정상 생성된다")
		void s3presigner_bean_is_created() {
			S3Presigner presigner = config.s3Presigner();
			assertThat(presigner).isNotNull();
			presigner.close();
		}
	}

	@Nested
	@DisplayName("MinIO 호환 설정")
	class MinioCompatibility {

		@Test
		@DisplayName("path-style access 가 활성화된다 (MinIO 호환)")
		void path_style_access_is_enabled() {
			S3Configuration s3conf = config.s3Configuration();
			assertThat(s3conf.pathStyleAccessEnabled())
				.as("MinIO 는 virtual-hosted-style 미지원이라 path-style 필수").isTrue();
		}

		@Test
		@DisplayName("endpoint 가 properties 값을 반영한다")
		void endpoint_uri_uses_property_value() {
			assertThat(config.endpointUri().toString()).isEqualTo(VALID_ENDPOINT);
		}
	}

	@Nested
	@DisplayName("Fail-safe 생성자 검증")
	class ConstructorValidation {

		@Test
		@DisplayName("endpoint 가 비어있으면 IllegalArgumentException")
		void blank_endpoint_throws() {
			assertThatThrownBy(() -> new S3Config(" ", VALID_REGION, VALID_ACCESS_KEY, VALID_SECRET_KEY))
				.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("region 이 비어있으면 IllegalArgumentException")
		void blank_region_throws() {
			assertThatThrownBy(() -> new S3Config(VALID_ENDPOINT, " ", VALID_ACCESS_KEY, VALID_SECRET_KEY))
				.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("accessKey 가 비어있으면 IllegalArgumentException")
		void blank_access_key_throws() {
			assertThatThrownBy(() -> new S3Config(VALID_ENDPOINT, VALID_REGION, " ", VALID_SECRET_KEY))
				.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		@DisplayName("secretKey 가 비어있으면 IllegalArgumentException")
		void blank_secret_key_throws() {
			assertThatThrownBy(() -> new S3Config(VALID_ENDPOINT, VALID_REGION, VALID_ACCESS_KEY, " "))
				.isInstanceOf(IllegalArgumentException.class);
		}
	}
}
