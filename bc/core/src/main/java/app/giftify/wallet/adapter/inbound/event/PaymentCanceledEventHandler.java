package app.giftify.wallet.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.shared.domain.event.payment.PaymentCanceledEvent;
import app.giftify.wallet.application.inbound.RestoreWalletCommand;
import app.giftify.wallet.application.inbound.RestoreWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCanceledEventHandler {
	private final RestoreWalletUseCase restoreWalletUseCase;

	@ApplicationModuleListener
	public void handle(PaymentCanceledEvent event) {
		if (!event.data().paymentMethod().isWalletPayment()) {
			return;
		}

		log.info("[PaymentCanceledEventHandler] Wallet 결제 취소 → 잔액 복원. memberId={}, cancelAmount={}",
			event.data().memberId(), event.data().amount());

		restoreWalletUseCase.restore(new RestoreWalletCommand(
			event.data().memberId(),
			event.data().amount(),
			event.data().transactionKey()
		));
	}
}
