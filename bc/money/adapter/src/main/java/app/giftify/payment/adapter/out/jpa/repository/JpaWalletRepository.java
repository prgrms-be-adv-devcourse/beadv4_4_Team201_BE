package app.giftify.payment.adapter.out.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaWalletRepository extends JpaRepository<JpaWallet, Long> {
}
