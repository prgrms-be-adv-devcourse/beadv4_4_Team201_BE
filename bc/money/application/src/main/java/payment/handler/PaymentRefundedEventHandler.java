package payment.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import app.giftify.shared.domain.event.payment.PaymentRefundedEvent;
import app.giftify.shared.domain.vo.Money;
import domain.payment.Payment;
import domain.payment.PaymentRepository;
import payment.usecase.PaymentInitiateUseCase;

/**
 * PaymentRefundedEvent 핸들러.
 * 결제 환불 시 예치금 복구를 처리합니다.
 * (내부 이벤트 처리이므로 Handler 네이밍 사용)
 */
@Component
public class PaymentRefundedEventHandler {

	private static final Logger log = LoggerFactory.getLogger(PaymentRefundedEventHandler.class);

	private final PaymentRepository paymentRepository;
	private final PaymentInitiateUseCase paymentInitiateUseCase;

	public PaymentRefundedEventHandler(
		PaymentRepository paymentRepository,
		PaymentInitiateUseCase paymentInitiateUseCase
	) {
		this.paymentRepository = paymentRepository;
		this.paymentInitiateUseCase = paymentInitiateUseCase;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(PaymentRefundedEvent event) {
		log.info("[WalletRefund] 결제 환불 이벤트 수신. paymentId={}", event.getPaymentId());

		Payment payment = paymentRepository.findById(event.getPaymentId())
			.orElse(null);

		if (payment == null) {
			log.warn("[WalletRefund] 결제를 찾을 수 없음. paymentId={}", event.getPaymentId());
			return;
		}

		Money walletUsedAmount = payment.getWalletUsedAmount();
		if (walletUsedAmount == null || !walletUsedAmount.isGreaterThan(Money.zero())) {
			log.info("[WalletRefund] 예치금 사용 내역 없음. paymentId={}", event.getPaymentId());
			return;
		}

		// 기존 rollbackWallet() 메서드 활용
		paymentInitiateUseCase.rollbackWallet(
			event.getUserId(),
			walletUsedAmount,
			event.getPaymentId()
		);

		log.info("[WalletRefund] 예치금 복구 완료. paymentId={}, amount={}",
			event.getPaymentId(), walletUsedAmount);
	}
}
