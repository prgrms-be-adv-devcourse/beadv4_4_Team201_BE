package app.giftify.payment.adapter.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
/**
 * Spring Security 기반 결제 필드 암호화 구현체.
 * AES-256-GCM 알고리즘을 사용합니다.
 */
@Component
public class SpringSecurityPaymentFieldEncryptor implements PaymentFieldEncryptor {
	private static final Logger log = LoggerFactory.getLogger(SpringSecurityPaymentFieldEncryptor.class);

	private final TextEncryptor encryptor;

	public SpringSecurityPaymentFieldEncryptor(
		@Value("${payment.encryption.password:dev-encryption-password}") String password,
		@Value("${payment.encryption.salt:deadbeefcafebabe}") String salt
	) {
		this.encryptor = Encryptors.delux(password, salt); //  Default + Lux(ury) 또는 Deluxe의 약자로, "고급 기본 암호화"를 의미
	}

	@Override
	public String encrypt(String plaintext) {
		if (plaintext == null || plaintext.isEmpty()) {
			return plaintext;
		}
		log.info("[PaymentFieldEncryptor] 민감정보 암호화 적용");
		return encryptor.encrypt(plaintext);
	}

	@Override
	public String decrypt(String ciphertext) {
		if (ciphertext == null || ciphertext.isEmpty()) {
			return ciphertext;
		}
		log.info("[PaymentFieldEncryptor] 민감정보 복호화 적용");
		return encryptor.decrypt(ciphertext);
	}
}
