package domain.wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findById(Long id);

    // todo: findByUserId(Long userId) -> findByMemberId(Long memberId)
    Optional<Wallet> findByUserId(Long userId);
}
