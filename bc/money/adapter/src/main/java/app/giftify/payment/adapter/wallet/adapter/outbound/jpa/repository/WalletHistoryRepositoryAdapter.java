package app.giftify.payment.adapter.wallet.adapter.outbound.jpa.repository;

import app.giftify.payment.adapter.out.jpa.entity.JpaWalletHistory;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import walletHistory.port.WalletHistoryRepository;

@Repository
@RequiredArgsConstructor
public class WalletHistoryRepositoryAdapter implements WalletHistoryRepository {

    private final JpaWalletHistoryRepository walletHistoryRepository;

    @Override
    public void record(Long walletId, String transactionType, Money amount, Money balanceAfter, String referenceType, Long referenceId) {
        walletHistoryRepository.save(
                new JpaWalletHistory(
                        walletId,
                        transactionType,
                        amount,
                        balanceAfter,
                        referenceType,
                        referenceId
                )
        );
    }
    @Override
    public boolean existsByReferenceIdAndReferenceType(Long referenceId, String referenceType) {
        return walletHistoryRepository.existsByReferenceIdAndReferenceType(referenceId, referenceType);
    }
}
