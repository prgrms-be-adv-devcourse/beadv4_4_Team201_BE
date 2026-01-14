package app.giftify.payment.adapter.out.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaWalletRepositoryImpl implements WalletRepository {

    private final JpaWalletRepository jpaWalletRepository;

    public JpaWalletRepositoryImpl(JpaWalletRepository jpaWalletRepository) {
        this.jpaWalletRepository = jpaWalletRepository;
    }

    // todo : save()가 Wallet을 반환해야 하는가?
    @Override
    public Wallet save(Wallet wallet) {
        JpaWallet entity = JpaWallet.from(wallet.snapshot());
        JpaWallet saved = jpaWalletRepository.save(entity);

        return Wallet.restore(saved.toSnapshot());
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return jpaWalletRepository.findById(id)
                .map(entity -> Wallet.restore(entity.toSnapshot()));
    }
}
