package app.giftify.wallet.domain;

import app.giftify.shared.domain.base.BaseDomainModel;
import app.giftify.shared.domain.vo.Money;

import java.time.LocalDateTime;

public class WalletHistory extends BaseDomainModel {

	private final Long walletId;
	private final TransactionType transactionType;
	private final Money amount;
	private final Money balanceAfter;
	private final ReferenceType referenceType;
	private final String referenceId;
	private final LocalDateTime occurredAt;

	private WalletHistory(
		Long id,
		Long walletId,
		TransactionType transactionType,
		Money amount,
		Money balanceAfter,
		ReferenceType referenceType,
		String referenceId,
		LocalDateTime occurredAt
	) {
		super(id);
		this.walletId = walletId;
		this.transactionType = transactionType;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
		this.occurredAt = occurredAt;
	}

	public static WalletHistory create(
		Long walletId,
		TransactionType transactionType,
		Money amount,
		Money balanceAfter,
		ReferenceType referenceType,
		String referenceId,
		LocalDateTime occurredAt
	) {
		validateNull(walletId, "walletId");
		validateNull(transactionType, "transactionType");
		validateNull(amount, "amount");
		validateNull(balanceAfter, "balanceAfter");
		validateNull(referenceType, "referenceType");
		validateNull(referenceId, "referenceId");
		validateNull(occurredAt, "occurredAt");

		return new WalletHistory(
			null,
			walletId,
			transactionType,
			amount,
			balanceAfter,
			referenceType,
			referenceId,
			occurredAt
		);
	}

	public static WalletHistory restore(
		Long id,
		Long walletId,
		TransactionType transactionType,
		Money amount,
		Money balanceAfter,
		ReferenceType referenceType,
		String referenceId,
		LocalDateTime occurredAt
	) {
		return new WalletHistory(
			id,
			walletId,
			transactionType,
			amount,
			balanceAfter,
			referenceType,
			referenceId,
			occurredAt
		);
	}

	public Long getWalletId() {
		return walletId;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public Money getAmount() {
		return amount;
	}

	public Money getBalanceAfter() {
		return balanceAfter;
	}

	public ReferenceType getReferenceType() {
		return referenceType;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	private static void validateNull(Object value, String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException("[WalletHistory] " + fieldName + "은(는) null일 수 없습니다.");
		}
	}
}
