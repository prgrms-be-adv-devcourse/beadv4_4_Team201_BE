package app.giftify.payment.adapter.wallet.domain;

import domain.wallet.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findById(Long id);

    Optional<Wallet> findByMemberId(Long userId);
}
