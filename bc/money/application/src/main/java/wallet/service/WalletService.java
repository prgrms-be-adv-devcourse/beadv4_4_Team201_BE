package wallet.service;

import domain.member.MoneyMember;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import vo.Money;
import wallet.usecase.WalletCreateUseCase;
import wallet.usecase.WalletGetBalanceUseCase;

@Service
public class WalletService implements WalletCreateUseCase, WalletGetBalanceUseCase {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Wallet createWallet(MoneyMember member) {
        return walletRepository.save(new Wallet(member));
    }

    @Override
    public Money getBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 지갑입니다."));

        return wallet.getBalance();
    }
}
