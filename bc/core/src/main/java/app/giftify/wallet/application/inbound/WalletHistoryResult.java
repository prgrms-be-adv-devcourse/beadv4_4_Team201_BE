package app.giftify.wallet.application.inbound;

import java.time.LocalDateTime;

import app.giftify.shared.domain.vo.Money;
import app.giftify.wallet.domain.ReferenceType;
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
			getDescription(history.getTransactionType(), history.getReferenceType()),
			history.getReferenceId(),
			history.getOccurredAt()
		);
	}

	private static String getDescription(TransactionType type, ReferenceType refType) {
		return switch (type) { // enum 안에 넣도록 변경하기
			case CHARGE -> "캐시 충전";
			case WITHDRAW -> "출금";
			case PAYMENT -> "결제";
			case SETTLEMENT_PAYOUT -> "정산 입금";
			case SETTLEMENT_CLAWBACK -> "정산 환수";
		};
	}
}
