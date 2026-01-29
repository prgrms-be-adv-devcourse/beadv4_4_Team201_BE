package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

public record WalletBalanceResult(
	Long walletId,
	Long memberId,
	Money balance
) {
}
