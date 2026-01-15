package domain.wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findById(Long id);

    Optional<Wallet> findByUserId(Long userId);
}
