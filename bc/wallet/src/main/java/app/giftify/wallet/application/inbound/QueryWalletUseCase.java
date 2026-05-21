package app.giftify.wallet.application.inbound;

public interface QueryWalletUseCase {
	WalletBalanceResult getBalance(Long memberId);
}
