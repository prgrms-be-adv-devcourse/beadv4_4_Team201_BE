package wallet.service;

import domain.member.MoneyMember;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.springframework.stereotype.Service;
import wallet.usecase.WalletCreateUseCase;

@Service
public class WalletService implements WalletCreateUseCase {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public Wallet createWallet(MoneyMember member) {
        return walletRepository.save(new Wallet(member));
    }
}
