package app.giftify.payment.domain;

/**
 * 결제 수단.
 * 각 결제 수단의 특성(PG 연동 필요 여부, 즉시 결제 여부)을 정의합니다.
 */
public enum PaymentMethod {
	GIFTIFY_POINT(false, true),     // 내부 포인트 - PG 불필요, 즉시 결제
	CARD(true, false),              // 신용/체크카드 - PG 필요, 승인 대기
	BANK_TRANSFER(true, false),     // 계좌이체 - PG 필요, 승인 대기
	VIRTUAL_ACCOUNT(true, false);   // 가상계좌 - PG 필요, 입금 대기

	private final boolean requiresPg;
	private final boolean instantPayment;

	PaymentMethod(boolean requiresPg, boolean instantPayment) {
		this.requiresPg = requiresPg;
		this.instantPayment = instantPayment;
	}

	/**
	 * PG사 연동이 필요한 결제 수단인지 여부
	 */
	public boolean requiresPg() {
		return requiresPg;
	}

	/**
	 * 즉시 결제가 완료되는 결제 수단인지 여부
	 * (예: 포인트는 즉시 차감, 카드/계좌이체는 PG 승인 필요)
	 */
	public boolean isInstantPayment() {
		return instantPayment;
	}
}
