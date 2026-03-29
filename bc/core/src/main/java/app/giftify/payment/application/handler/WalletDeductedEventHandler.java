package app.giftify.payment.application.handler;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import app.giftify.payment.application.outbound.PaymentRepository;
import app.giftify.payment.domain.Payment;
import app.giftify.payment.domain.PaymentErrorCode;
import app.giftify.payment.domain.PaymentException;
import app.giftify.shared.domain.event.EventPublisher;

import app.giftify.wallet.domain.event.WalletDeductedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Wallet 결제 차감 완료 이벤트 핸들러.
 * WalletDeductedEvent를 수신하여 Payment 상태를 PAID로 변경합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletDeductedEventHandler {
	private final PaymentRepository paymentRepository;
	private final EventPublisher eventPublisher;

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

		var domainEvents = paid.pullEvents();
		paymentRepository.save(paid);
		domainEvents.forEach(eventPublisher::publish);

		log.info("[WalletDeductedEventHandler] Payment 결제 완료 처리. paymentId={}, status={}",
				paid.getId(), paid.getStatus());
	}
}
