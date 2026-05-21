package app.giftify.payment.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.PaymentModuleEventPublisher;
import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;

import app.giftify.wallet.domain.event.WalletDeductedEvent;

import lombok.RequiredArgsConstructor;
/**
 * Wallet 결제 차감 완료 이벤트 핸들러.
 * WalletDeductedEvent를 수신하여 Payment 상태를 PAID로 변경합니다.
 */
@Component
@RequiredArgsConstructor
public class WalletDeductedEventHandler {
	private static final Logger log = LoggerFactory.getLogger(WalletDeductedEventHandler.class);

	private final PaymentRepository paymentRepository;
	private final PaymentModuleEventPublisher moduleEventPublisher;

	@EventListener
	@Transactional
	public void handle(WalletDeductedEvent event) {
		log.info("[WalletDeductedEventHandler] 이벤트 수신. paymentId={}, walletId={}, amount={}",
			event.getPaymentId(), event.getWalletId(), event.getAmount());

		Payment payment = paymentRepository.findById(event.getPaymentId())
			.orElseThrow(() -> new PaymentException(
				PaymentErrorCode.PAYMENT_NOT_FOUND,
				"[WalletDeductedEventHandler] Payment를 찾을 수 없습니다. paymentId=" + event.getPaymentId()
			));

		Payment paid = payment.complete(null, null, null, event.getDeductedAt());
		paymentRepository.save(paid);
		moduleEventPublisher.publishFrom(paid, payment);

		log.info("[WalletDeductedEventHandler] Payment 결제 완료 처리. paymentId={}, status={}",
				paid.getId(), paid.getStatus());
	}
}
