package app.giftify.wallet.application.inbound;

public interface DeductWalletUseCase {
	DeductWalletResult deductForPayment(DeductWalletCommand command);
}
