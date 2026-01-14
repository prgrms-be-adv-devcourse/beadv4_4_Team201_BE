package wallet.usecase;

import vo.Money;

public interface WalletGetBalanceUseCase {
    Money getBalance(Long walletId);
}
