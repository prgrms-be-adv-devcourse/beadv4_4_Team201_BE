package app.giftify.wallet.application.inbound;

import app.giftify.support.common.money.Money;

public record WalletBalanceResult(
	Long walletId,
	Long memberId,
	Money balance
) {
}
