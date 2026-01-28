package app.giftify.payment.adapter.wallet.adapter.outbound.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaWalletRepository extends JpaRepository<JpaWallet, Long> {
    Optional<JpaWallet> findByMemberId(Long memberId);
}
