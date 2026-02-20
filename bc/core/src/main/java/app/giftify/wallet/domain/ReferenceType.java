package app.giftify.wallet.domain;

/**
 *  멱등성 키 네임스페이스
 *  어떤 외부 엔티티가 트리거했나
 */
public enum ReferenceType {
	CHARGE,
	WITHDRAWAL,
	PAYMENT,
	SETTLEMENT
}
