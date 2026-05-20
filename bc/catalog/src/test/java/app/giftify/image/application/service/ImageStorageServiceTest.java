package app.giftify.image.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import app.giftify.image.application.port.in.IssuePresignedDownloadUseCase;
import app.giftify.image.application.port.in.IssuePresignedUploadUseCase;
import app.giftify.image.application.port.out.ImageStoragePort;
import app.giftify.image.application.port.out.ImageStoragePort.PresignedUrl;

class ImageStorageServiceTest {

	private ImageStoragePort storage;
	private ImageStorageService service;

	@BeforeEach
	void setUp() {
		storage = Mockito.mock(ImageStoragePort.class);
		service = new ImageStorageService(storage);
	}

	@Nested
	@DisplayName("PresignedUpload 발급")
	class IssueUpload {

		@Test
		@DisplayName("생성된 key 와 port 가 반환한 url 이 결합되어 반환된다")
		void issues_upload_with_generated_key() {
			given(storage.presignPut(any(), eq("image/jpeg"), eq(Duration.ofMinutes(5))))
				.willReturn(new PresignedUrl("https://minio:9000/upload?sig=...", "PUT", Duration.ofMinutes(5)));

			var command = new IssuePresignedUploadUseCase.Command(
				"products", 42L, "image/jpeg", Duration.ofMinutes(5));
			var result = service.issue(command);

			assertThat(result.key()).startsWith("products/42/").endsWith(".jpg");
			assertThat(result.url()).isEqualTo("https://minio:9000/upload?sig=...");
			assertThat(result.httpMethod()).isEqualTo("PUT");
			assertThat(result.expiry()).isEqualTo(Duration.ofMinutes(5));
		}

		@Test
		@DisplayName("port 에 전달되는 key 와 결과 key 가 동일하다")
		void key_passed_to_port_matches_result_key() {
			given(storage.presignPut(any(), any(), any()))
				.willReturn(new PresignedUrl("https://x", "PUT", Duration.ofMinutes(5)));

			var command = new IssuePresignedUploadUseCase.Command(
				"products", 7L, "image/png", Duration.ofMinutes(3));
			var result = service.issue(command);

			ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
			Mockito.verify(storage).presignPut(keyCaptor.capture(), eq("image/png"), eq(Duration.ofMinutes(3)));
			assertThat(keyCaptor.getValue()).isEqualTo(result.key());
		}

		@Test
		@DisplayName("지원하지 않는 contentType 은 IllegalArgumentException")
		void unsupported_content_type_throws() {
			var command = new IssuePresignedUploadUseCase.Command(
				"products", 1L, "application/pdf", Duration.ofMinutes(5));
			assertThatThrownBy(() -> service.issue(command))
				.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Nested
	@DisplayName("PresignedDownload 발급")
	class IssueDownload {

		@Test
		@DisplayName("port 가 반환한 url 을 그대로 통과시킨다")
		void delegates_to_port() {
			given(storage.presignGet("products/1/abc.jpg", Duration.ofMinutes(2)))
				.willReturn(new PresignedUrl("https://minio:9000/get?sig=...", "GET", Duration.ofMinutes(2)));

			var command = new IssuePresignedDownloadUseCase.Command(
				"products/1/abc.jpg", Duration.ofMinutes(2));
			var result = service.issue(command);

			assertThat(result.key()).isEqualTo("products/1/abc.jpg");
			assertThat(result.url()).isEqualTo("https://minio:9000/get?sig=...");
			assertThat(result.httpMethod()).isEqualTo("GET");
		}
	}
}
