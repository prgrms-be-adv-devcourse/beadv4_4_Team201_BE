package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.domain.ReferenceType;
import app.giftify.wallet.domain.TransactionType;
import app.giftify.wallet.domain.WalletHistory;

import java.time.LocalDateTime;

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
			getDescription(history.getTransactionType(), history.getReferenceType()),
			history.getReferenceId(),
			history.getOccurredAt()
		);
	}

	private static String getDescription(TransactionType type, ReferenceType refType) {
		return switch (type) {
			case CHARGE -> "캐시 충전";
			case WITHDRAW -> "출금";
			case PAYMENT -> "결제";
		};
	}
}
