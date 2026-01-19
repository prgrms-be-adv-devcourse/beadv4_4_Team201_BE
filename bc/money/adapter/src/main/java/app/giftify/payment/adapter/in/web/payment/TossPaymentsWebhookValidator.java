package app.giftify.payment.adapter.in.web.payment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossPaymentsWebhookValidator implements PgWebhookValidator {
	private static final Logger log = LoggerFactory.getLogger(TossPaymentsWebhookValidator.class);
	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final String SIGNATURE_PREFIX = "v1:";

	@Value("${tosspayments.webhook.secret-key}")
	private String secretKey;

	@Override
	public boolean validate(String payload, String timestamp, String signature) {
		if (secretKey == null || secretKey.isBlank()) {
			log.warn("[Toss-Webhook] Secret key가 설정되지 않았습니다.");
			return false;
		}

		if (signature == null || !signature.startsWith(SIGNATURE_PREFIX)) {
			log.warn("[Toss-Webhook] 유효하지 않은 서명 형식입니다.");
			return false;
		}

		try {
			String expectedSignature = generateSignature(timestamp, payload);
			String actualSignature = signature.substring(SIGNATURE_PREFIX.length());

			boolean isValid = MessageDigest.isEqual(
				expectedSignature.getBytes(),
				actualSignature.getBytes()
			);

			if (!isValid) {
				log.warn("[Toss-Webhook] 서명 불일치. expected={}, actual={}", expectedSignature, actualSignature);
			}
			return isValid;

		} catch (Exception e) {
			log.error("[Toss-Webhook] 서명 검증 중 오류 발생", e);
			return false;
		}
	}

	private String generateSignature(String timestamp, String payload) throws Exception {
		String message = timestamp + payload;

		Mac mac = Mac.getInstance(HMAC_ALGORITHM);
		SecretKeySpec keySpec = new SecretKeySpec(
			secretKey.getBytes(StandardCharsets.UTF_8),
			HMAC_ALGORITHM
		);
		mac.init(keySpec);

		byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(hash);
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
