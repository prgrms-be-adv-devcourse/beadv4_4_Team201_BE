package app.giftify.wallet.application.outbound;

import app.giftify.wallet.domain.Wallet;

import java.util.Optional;

public interface WalletRepository {
	Optional<Wallet> findById(Long id);
	Optional<Wallet> findByMemberId(Long memberId);
	Wallet save(Wallet wallet);
}
