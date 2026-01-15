package wallet.usecase;

import domain.wallet.Wallet;

public interface WalletQueryUseCase {
    Wallet getWallet(Long walletId);
}
