package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

public record DeductWalletCommand(
	Long memberId,
	Long paymentId,
	String orderId,
	Money amount
) {
}
