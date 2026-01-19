package walletHistory.port;

import app.giftify.shared.domain.vo.Money;

public interface WalletHistoryRepository {

    void record(
            Long walletId,
            String transactionType,
            Money amount,
            Money balanceAfter,
            String referenceType,
            Long referenceId
    );

    boolean existsByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}
