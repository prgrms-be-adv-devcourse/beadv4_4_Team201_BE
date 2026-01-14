package wallet.usecase;

import domain.wallet.Wallet;

public interface WalletCreateUseCase {

    Wallet createWallet(Long memberId);
}
