package app.giftify.payment.application.outbound;

public interface PaymentFieldEncryptor {
	String encrypt(String plaintext);

	String decrypt(String ciphertext);
}
