package walletHistory.recorder;

import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import walletHistory.port.WalletHistoryPort;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WalletHistoryRecorder {

    private final WalletHistoryPort walletHistoryPort;

    public void record(
            Long walletId,
            String transactionType,
            Money amount,
            Money balanceAfter,
            String referenceType,
            Long referenceId
    ) {
        walletHistoryPort.record(
                walletId,
                transactionType,
                amount,
                balanceAfter,
                referenceType,
                referenceId
        );
    }
}
