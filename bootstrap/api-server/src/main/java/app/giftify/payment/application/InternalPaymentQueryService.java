package app.giftify.payment.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.inbound.InternalPaymentQueryUseCase;
import app.giftify.payment.application.inbound.InternalPaymentResult;
import app.giftify.payment.application.outbound.PaymentFieldEncryptor;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
@Service
@Transactional(readOnly = true)
public class InternalPaymentQueryService implements InternalPaymentQueryUseCase {
	private static final Logger log = LoggerFactory.getLogger(InternalPaymentQueryService.class);


	private final PaymentRepository paymentRepository;
	private final PaymentFieldEncryptor encryptor;

	public InternalPaymentQueryService(
		PaymentRepository paymentRepository,
		PaymentFieldEncryptor encryptor
	) {
		this.paymentRepository = paymentRepository;
		this.encryptor = encryptor;
	}

	@Override
	public Optional<InternalPaymentResult> findById(Long paymentId) {
		log.debug("[InternalPaymentQueryService] 결제 조회. paymentId={}", paymentId);

		return paymentRepository.findById(paymentId)
			.map(this::toResultWithDecryption);
	}

	@Override
	public Optional<InternalPaymentResult> findByOrderNumber(String orderNumber) {
		log.debug("[InternalPaymentQueryService] 주문별 결제 조회. orderNumber={}", orderNumber);

		return paymentRepository.findByOrderNumber(orderNumber)
			.map(this::toResultWithDecryption);
	}

	/**
	 * Payment를 복호화된 InternalPaymentResult로 변환
	 */
	private InternalPaymentResult toResultWithDecryption(Payment payment) {
		String decryptedPaymentKey = decryptIfPresent(payment.getPaymentKey());
		String decryptedApproveCode = decryptIfPresent(payment.getApproveCode());

		return InternalPaymentResult.of(payment, decryptedPaymentKey, decryptedApproveCode);
	}

	/**
	 * 값이 존재하면 복호화, null이면 null 반환.
	 *
	 * @throws PaymentException 복호화 실패 시
	 */
	private String decryptIfPresent(String encryptedValue) {
		if (encryptedValue == null || encryptedValue.isBlank()) {
			return null;
		}
		try {
			return encryptor.decrypt(encryptedValue);
		} catch (Exception e) {
			log.error("[InternalPaymentQueryService] 복호화 실패", e);
			throw new PaymentException(PaymentErrorCode.DECRYPTION_FAILED, "결제 정보 복호화 실패", e);
		}
	}
}
