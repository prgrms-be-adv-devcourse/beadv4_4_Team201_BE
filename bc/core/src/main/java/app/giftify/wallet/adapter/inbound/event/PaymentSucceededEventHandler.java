package app.giftify.wallet.adapter.inbound.event;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import app.giftify.shared.domain.event.payment.PaymentSucceededEvent;
import app.giftify.shared.domain.type.PaymentType;
import app.giftify.wallet.application.inbound.ChargeWalletCommand;
import app.giftify.wallet.application.inbound.ChargeWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSucceededEventHandler {
	private final ChargeWalletUseCase chargeWalletUseCase;

	@ApplicationModuleListener
	public void handle(PaymentSucceededEvent event) {
		if (event.data().paymentType() != PaymentType.DEPOSIT_CHARGE) {
			return;
		}

		log.info("[PaymentSucceededEventHandler] memberId={}, amount={}",
			event.data().memberId(), event.data().paidAmount());

		chargeWalletUseCase.charge(new ChargeWalletCommand(
			event.data().memberId(),
			event.data().paidAmount(),
			event.data().orderNumber()
		));
	}
}
