package app.giftify.wallet.application.inbound;

/**
 * 지갑 출금 UseCase
 */
public interface WithdrawWalletUseCase {
	WithdrawWalletResult withdraw(WithdrawWalletCommand command);
}
