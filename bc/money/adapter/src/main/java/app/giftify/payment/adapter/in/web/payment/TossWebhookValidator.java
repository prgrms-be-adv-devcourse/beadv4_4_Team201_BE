package app.giftify.payment.adapter.in.web.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TossWebhookValidator implements PgWebhookValidator {
	private static final Logger log = LoggerFactory.getLogger(TossWebhookValidator.class);

	@Value("${pg.toss.webhook.secret-key}")
	private String secretKey;

	@Override
	public boolean validate(String payload, String timestamp, String signatureHeader) {
		if (signatureHeader == null || timestamp == null) {
			return false;
		}

		try {
			String data = payload + ":" + timestamp;
			Mac hmacSha256 = Mac.getInstance("HmacSHA256");
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			hmacSha256.init(secretKeySpec);
			byte[] hashBytes = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));

			String[] parts = signatureHeader.split(",");
			for (String part : parts) {
				if (part.trim().startsWith("v1:")) {
					String encodedSign = part.trim().substring(3);
					byte[] decodedSign = Base64.getDecoder().decode(encodedSign);
					if (java.util.Arrays.equals(hashBytes, decodedSign)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.error("Toss Signature validation failed", e);
		}
		return false;
	}
}
