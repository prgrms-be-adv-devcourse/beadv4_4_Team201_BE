package app.giftify.image.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageKeyGeneratorTest {

	@Test
	@DisplayName("키는 {domain}/{ownerId}/{uuid}.{ext} 패턴을 따른다")
	void key_matches_pattern() {
		String key = ImageKeyGenerator.generate("products", 42L, "image/jpeg");
		assertThat(key).matches("^products/42/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg$");
	}

	@Test
	@DisplayName("image/png 은 .png 확장자")
	void png_extension() {
		assertThat(ImageKeyGenerator.generate("profiles", 1L, "image/png")).endsWith(".png");
	}

	@Test
	@DisplayName("image/webp 은 .webp 확장자")
	void webp_extension() {
		assertThat(ImageKeyGenerator.generate("profiles", 1L, "image/webp")).endsWith(".webp");
	}

	@Test
	@DisplayName("같은 입력이라도 UUID 가 달라 key 가 매번 다르다")
	void unique_per_call() {
		String a = ImageKeyGenerator.generate("products", 1L, "image/jpeg");
		String b = ImageKeyGenerator.generate("products", 1L, "image/jpeg");
		assertThat(a).isNotEqualTo(b);
	}

	@Test
	@DisplayName("지원하지 않는 contentType 은 IllegalArgumentException")
	void unsupported_content_type_throws() {
		assertThatThrownBy(() -> ImageKeyGenerator.generate("products", 1L, "application/pdf"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("domain 이 빈 문자열이면 IllegalArgumentException")
	void blank_domain_throws() {
		assertThatThrownBy(() -> ImageKeyGenerator.generate(" ", 1L, "image/jpeg"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("ownerId 가 null 이면 IllegalArgumentException")
	void null_owner_throws() {
		assertThatThrownBy(() -> ImageKeyGenerator.generate("products", null, "image/jpeg"))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
