package app.giftify.wallet.application.inbound;

import app.giftify.support.common.money.Money;

public record DeductWalletCommand(
	Long memberId,
	Long paymentId,
	String orderId,
	Money amount
) {
}
