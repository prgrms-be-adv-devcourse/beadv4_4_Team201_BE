package app.giftify.payment.adapter.out.jpa.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import domain.exception.WalletNotFoundException;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;

@Repository
public class WalletRepositoryAdapter implements WalletRepository {

    private final JpaWalletRepository jpaWalletRepository;

    public WalletRepositoryAdapter(JpaWalletRepository jpaWalletRepository) {
        this.jpaWalletRepository = jpaWalletRepository;
    }

    // todo : save()가 Wallet을 반환해야 하는가?
    @Override
    public Wallet save(Wallet wallet) {
        if (wallet.getId() == null) {
            JpaWallet entity = JpaWallet.from(wallet.snapshot());
            return Wallet.restore(jpaWalletRepository.save(entity).toSnapshot());
        }

        JpaWallet entity = jpaWalletRepository.findById(wallet.getId())
                .orElseThrow(() -> new WalletNotFoundException(wallet.getId()));

        entity.updateFrom(wallet.snapshot());

        return Wallet.restore(entity.toSnapshot());
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return jpaWalletRepository.findById(id)
                .map(entity -> Wallet.restore(entity.toSnapshot()));
    }

    @Override
    public Optional<Wallet> findByMemberId(Long memberId) {
        return jpaWalletRepository.findByMemberId(memberId)
                .map(entity -> Wallet.restore(entity.toSnapshot()));
    }
}
