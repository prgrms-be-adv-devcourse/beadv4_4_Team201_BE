package app.giftify.wallet.application.outbound;

import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.WalletHistory;

public interface WalletHistoryRepository {
	void record(WalletHistory history);
	boolean existsByReferenceIdAndReferenceType(String referenceId, ReferenceType type);
}
