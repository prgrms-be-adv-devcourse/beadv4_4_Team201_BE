package app.giftify.payment.adapter.out.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaMoneyMember;
import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import domain.member.MoneyMember;
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

    @Override
    public Wallet save(Wallet wallet) {
        JpaMoneyMember jpaMoneyMember = new JpaMoneyMember(wallet.getId());
        JpaWallet jpaWallet = new JpaWallet(jpaMoneyMember, wallet.getBalance());

        JpaWallet savedJpaWallet = jpaWalletRepository.save(jpaWallet);

        return new Wallet(
                savedJpaWallet.getId(),
                new MoneyMember(savedJpaWallet.getMember().getId()),
                savedJpaWallet.getBalance(),
                savedJpaWallet.getCreatedAt(),
                savedJpaWallet.getModifiedAt()
        );
    }

    @Override
    public Optional<Wallet> findById(Long id) {
        return jpaWalletRepository.findById(id)
                .map(jpaWallet -> new Wallet(
                        jpaWallet.getId(),
                        new MoneyMember(jpaWallet.getMember().getId()),
                        jpaWallet.getBalance(),
                        jpaWallet.getCreatedAt(),
                        jpaWallet.getModifiedAt()
                ));
    }
}
