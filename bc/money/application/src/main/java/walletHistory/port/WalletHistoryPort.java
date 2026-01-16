package walletHistory.port;

import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public interface WalletHistoryPort {

    void record(
            Long walletId,
            String transactionType,
            Money amount,
            Money balanceAfter,
            String referenceType,
            Long referenceId
    );
}
