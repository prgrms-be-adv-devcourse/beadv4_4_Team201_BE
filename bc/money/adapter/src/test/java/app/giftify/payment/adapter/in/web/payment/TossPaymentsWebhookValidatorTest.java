package app.giftify.payment.adapter.in.web.payment;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = TossPaymentsWebhookValidator.class)
@TestPropertySource(properties = "tosspayments.webhook.secret-key=test-secret")
class TossPaymentsWebhookValidatorTest {

	@Autowired
	private TossPaymentsWebhookValidator validator;

	@Test
	@DisplayName("올바른 서명이면 true 반환")
	void validate_ShouldReturnTrue_WhenSignatureIsValid() {
		// Given
		String payload = "{\"key\":\"value\"}";
		String timestamp = "2024-01-01T12:00:00";
		String expectedSig = "v1:" + computeHmac("test-secret", timestamp + payload);

		// When & Then
		assertThat(validator.validate(payload, timestamp, expectedSig)).isTrue();
	}

	@Test
	@DisplayName("잘못된 서명이면 false 반환")
	void validate_ShouldReturnFalse_WhenSignatureIsInvalid() {
		assertThat(validator.validate("{}", "time", "v1:wrong")).isFalse();
	}

	@Test
	@DisplayName("v1: 접두사 없으면 false 반환")
	void validate_ShouldReturnFalse_WhenPrefixMissing() {
		assertThat(validator.validate("{}", "time", "no-prefix")).isFalse();
	}

	/**
	 * 테스트용 임시 유틸 메서드
	 */
	private String computeHmac(String secretKey, String message) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec = new SecretKeySpec(
				secretKey.getBytes(StandardCharsets.UTF_8),
				"HmacSHA256"
			);
			mac.init(keySpec);
			byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

			// Hex string 변환
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
