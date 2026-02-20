package app.giftify.wallet.domain;

/**
 * 거래 유형 분류 (조회/필터링)
 *  지갑에 무슨 일이 일어났나
 */
public enum TransactionType {
	CHARGE,
	WITHDRAW,
	PAYMENT,
	SETTLEMENT_PAYOUT
}
