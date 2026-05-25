package app.giftify.wallet.application.inbound;

import app.giftify.support.common.money.Money;

/**
 * 지갑 출금 결과
 */
public record WithdrawWalletResult(
	Long walletId,
	Long memberId,
	Money balanceAfter,
	Money withdrawnAmount,
	String transactionId,
	WithdrawStatus status
) {
}
