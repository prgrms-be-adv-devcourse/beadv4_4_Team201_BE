package app.giftify.wallet.application.inbound;

import java.time.LocalDateTime;

import app.giftify.support.common.money.Money;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.WalletHistory;

public record WalletHistoryResult(
	Long id,
	TransactionType type,
	Money amount,
	Money balanceAfter,
	String description,
	String relatedId,
	LocalDateTime createdAt
) {
	public static WalletHistoryResult from(WalletHistory history) {
		return new WalletHistoryResult(
			history.getId(),
			history.getTransactionType(),
			history.getAmount(),
			history.getBalanceAfter(),
			history.getTransactionType().getDescription(),
			history.getReferenceId(),
			history.getOccurredAt()
		);
	}
}
