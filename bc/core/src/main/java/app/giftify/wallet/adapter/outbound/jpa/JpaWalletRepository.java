package app.giftify.wallet.adapter.outbound.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.giftify.wallet.adapter.outbound.jpa.entity.JpaWallet;

public interface JpaWalletRepository extends JpaRepository<JpaWallet, Long> {
	Optional<JpaWallet> findByMemberId(Long memberId);
}
