package app.giftify.wallet.domain;

/**
 * 거래 유형 분류 (조회/필터링)
 * 지갑에 무슨 일이 일어났나
 */
public enum TransactionType {
	CHARGE("캐시 충전"),
	WITHDRAW("출금"),
	PAYMENT("결제"),
	SETTLEMENT_PAYOUT("정산 입금"),
	SETTLEMENT_CLAWBACK("정산 환수");

	private final String description;

	TransactionType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
