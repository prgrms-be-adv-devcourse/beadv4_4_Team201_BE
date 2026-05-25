package app.giftify.wallet.application.outbound;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.giftify.support.common.money.Money;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.WalletHistory;

public interface WalletHistoryRepository {
	void record(WalletHistory history);

	boolean existsByReferenceIdAndReferenceType(String referenceId, ReferenceType type);

	Page<WalletHistory> findByWalletId(Long walletId, TransactionType type, Pageable pageable);

	default void recordTransaction(Long walletId, TransactionType txType, Money amount,
		Money balanceAfter, ReferenceType refType, String referenceId) {
		record(WalletHistory.create(walletId, txType, amount, balanceAfter, refType, referenceId, LocalDateTime.now()));
	}
}
