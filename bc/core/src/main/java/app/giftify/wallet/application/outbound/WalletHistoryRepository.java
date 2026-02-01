package app.giftify.wallet.application.outbound;

import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.WalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletHistoryRepository {
	void record(WalletHistory history);
	boolean existsByReferenceIdAndReferenceType(String referenceId, ReferenceType type);
	Page<WalletHistory> findByWalletId(Long walletId, TransactionType type, Pageable pageable);
}
