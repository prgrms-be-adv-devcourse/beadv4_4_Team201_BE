package app.giftify.payment.adapter.out.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaMoneyMember;
import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import domain.member.MoneyMember;
import domain.wallet.Wallet;
import domain.wallet.WalletRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaWalletRepositoryImpl implements WalletRepository {

    private final JpaWalletRepository jpaWalletRepository;

    public JpaWalletRepositoryImpl(JpaWalletRepository jpaWalletRepository) {
        this.jpaWalletRepository = jpaWalletRepository;
    }

    @Override
    public Wallet save(Wallet wallet) {
        JpaMoneyMember jpaMoneyMember = new JpaMoneyMember(wallet.getId());
        JpaWallet jpaWallet = new JpaWallet(jpaMoneyMember);

        JpaWallet savedJpaWallet = jpaWalletRepository.save(jpaWallet);

        return new Wallet(
                savedJpaWallet.getId(),
                new MoneyMember(savedJpaWallet.getMember().getId()),
                savedJpaWallet.getBalance(),
                savedJpaWallet.getCreatedAt(),
                savedJpaWallet.getModifiedAt()
        );
    }
}
