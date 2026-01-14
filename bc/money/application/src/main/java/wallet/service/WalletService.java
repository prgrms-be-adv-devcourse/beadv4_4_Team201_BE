package wallet.service;

import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import vo.Money;
import wallet.usecase.WalletCreateUseCase;
import wallet.usecase.WalletQueryUseCase;

@Service
public class WalletService implements WalletCreateUseCase, WalletQueryUseCase {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Wallet createWallet(Long memberId) {
        Wallet wallet = Wallet.create(memberId, Money.zero());

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWallet(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 지갑입니다."));
    }
}
