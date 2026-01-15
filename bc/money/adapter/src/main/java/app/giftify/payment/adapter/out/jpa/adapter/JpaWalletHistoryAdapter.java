package app.giftify.payment.adapter.out.jpa.adapter;

import app.giftify.payment.adapter.out.jpa.entity.JpaWalletHistory;
import app.giftify.payment.adapter.out.jpa.repository.JpaWalletHistoryRepository;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import walletHistory.port.WalletHistoryPort;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JpaWalletHistoryAdapter implements WalletHistoryPort {

    private final JpaWalletHistoryRepository walletHistoryRepository;

    @Override
    public void record(
            Long walletId,
            String transactionType,
            Money amount,
            Money balanceAfter,
            String referenceType,
            Long referenceId,
            LocalDateTime occurredAt
    ) {
        JpaWalletHistory walletHistory = new JpaWalletHistory(
                walletId,
                transactionType,
                amount,
                balanceAfter,
                referenceType,
                referenceId,
                occurredAt
        );

        walletHistoryRepository.save(walletHistory);
    }
}
