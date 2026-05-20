package app.giftify.wallet.domain;

/**
 *  멱등성 키 네임스페이스
 *  어떤 외부 엔티티가 트리거했나
 */
public enum ReferenceType {
	CHARGE("충전"),
	WITHDRAWAL("출금"),
	PAYMENT("결제"),
	SETTLEMENT("정산"),
	CANCEL("취소"),
	PAYMENT_COMPENSATION("결제 보상");

	private final String description;

	ReferenceType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
