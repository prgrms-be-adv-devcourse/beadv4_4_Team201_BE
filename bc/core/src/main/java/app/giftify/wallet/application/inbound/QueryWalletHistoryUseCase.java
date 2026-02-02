package app.giftify.wallet.application.inbound;

import org.springframework.data.domain.Page;

public interface QueryWalletHistoryUseCase {
	Page<WalletHistoryResult> getHistory(WalletHistoryQuery query);
}
