package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

public record DeductWalletResult(
	Long walletId,
	Money balanceAfter,
	boolean success,
	String errorCode,
	Money requiredAmount,
	Money currentBalance
) {

	public static DeductWalletResult success(Long walletId, Money balanceAfter) {
		return new DeductWalletResult(
			walletId,
			balanceAfter,
			true,
			null,
			null,
			null
		);
	}

	public static DeductWalletResult insufficientBalance(Long walletId, Money requiredAmount, Money currentBalance) {
		return new DeductWalletResult(
			walletId,
			null,
			false,
			"INSUFFICIENT_BALANCE",
			requiredAmount,
			currentBalance
		);
	}
}
