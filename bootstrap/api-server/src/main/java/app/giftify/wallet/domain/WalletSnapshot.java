package app.giftify.wallet.domain;

import app.giftify.support.common.money.Money;

public record WalletSnapshot(
	Long id,
	Long memberId,
	Money balance
) {
}
