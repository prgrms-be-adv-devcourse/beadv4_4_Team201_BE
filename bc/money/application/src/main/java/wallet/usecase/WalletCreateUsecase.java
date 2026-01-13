package wallet.usecase;

import domain.wallet.Wallet;

public interface WalletCreateUsecase {

	Wallet createWallet(Long userId);
}
