package wallet.usecase;

import domain.wallet.Wallet;
import vo.Money;

public interface WalletQueryUseCase {
    Wallet getWallet(Long walletId);
}
