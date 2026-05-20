package app.giftify.image.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.net.URL;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import app.giftify.image.application.port.out.ImageStoragePort.PresignedUrl;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class AwsS3ImageStorageAdapterTest {

	private static final String BUCKET = "giftify-images";

	private S3Presigner presigner;
	private AwsS3ImageStorageAdapter adapter;

	@BeforeEach
	void setUp() {
		presigner = Mockito.mock(S3Presigner.class);
		adapter = new AwsS3ImageStorageAdapter(presigner, BUCKET);
	}

	@Nested
	@DisplayName("presignPut")
	class PresignPut {

		@Test
		@DisplayName("S3Presigner.presignPutObject 를 호출하고 결과 URL 을 PUT 메서드로 매핑한다")
		void delegates_to_s3_presigner_put() throws Exception {
			PresignedPutObjectRequest mockResult = Mockito.mock(PresignedPutObjectRequest.class);
			URL url = new URL("https://minio:9000/giftify-images/products/1/abc.jpg?sig=xyz");
			given(mockResult.url()).willReturn(url);
			given(presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(mockResult);

			PresignedUrl out = adapter.presignPut("products/1/abc.jpg", "image/jpeg", Duration.ofMinutes(5));

			assertThat(out.url()).isEqualTo(url.toString());
			assertThat(out.httpMethod()).isEqualTo("PUT");
			assertThat(out.expiry()).isEqualTo(Duration.ofMinutes(5));
		}

		@Test
		@DisplayName("bucket 과 key 를 putObject 요청에 전달한다")
		void puts_bucket_and_key_into_request() throws Exception {
			URL url = new URL("https://x");
			PresignedPutObjectRequest mockResult = Mockito.mock(PresignedPutObjectRequest.class);
			given(mockResult.url()).willReturn(url);
			given(presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(mockResult);

			adapter.presignPut("products/1/abc.jpg", "image/jpeg", Duration.ofMinutes(5));

			ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
			Mockito.verify(presigner).presignPutObject(captor.capture());
			PutObjectPresignRequest req = captor.getValue();
			assertThat(req.putObjectRequest().bucket()).isEqualTo(BUCKET);
			assertThat(req.putObjectRequest().key()).isEqualTo("products/1/abc.jpg");
			assertThat(req.putObjectRequest().contentType()).isEqualTo("image/jpeg");
			assertThat(req.signatureDuration()).isEqualTo(Duration.ofMinutes(5));
		}
	}

	@Nested
	@DisplayName("presignGet")
	class PresignGet {

		@Test
		@DisplayName("S3Presigner.presignGetObject 를 호출하고 결과 URL 을 GET 메서드로 매핑한다")
		void delegates_to_s3_presigner_get() throws Exception {
			PresignedGetObjectRequest mockResult = Mockito.mock(PresignedGetObjectRequest.class);
			URL url = new URL("https://minio:9000/giftify-images/products/1/abc.jpg?sig=zzz");
			given(mockResult.url()).willReturn(url);
			given(presigner.presignGetObject(any(GetObjectPresignRequest.class))).willReturn(mockResult);

			PresignedUrl out = adapter.presignGet("products/1/abc.jpg", Duration.ofMinutes(2));

			assertThat(out.url()).isEqualTo(url.toString());
			assertThat(out.httpMethod()).isEqualTo("GET");
			assertThat(out.expiry()).isEqualTo(Duration.ofMinutes(2));
		}
	}

	@Nested
	@DisplayName("생성자 검증")
	class ConstructorValidation {

		@Test
		@DisplayName("bucket 이 비어있으면 IllegalArgumentException")
		void blank_bucket_throws() {
			org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
				() -> new AwsS3ImageStorageAdapter(presigner, " "));
		}
	}
}
