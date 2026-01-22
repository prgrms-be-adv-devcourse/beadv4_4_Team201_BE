package payment.usecase.result;

import app.giftify.shared.domain.vo.Money;

/**
 * 결제 시작 결과.
 * 예치금 사용액, PG 결제 필요액, 완료 여부 등을 포함합니다.
 */
public record PaymentInitiateResult(
	Long orderId,               // Order BC의 주문 ID
	Money walletUsed,           // 예치금 사용액
	Money pgPaymentRequired,    // PG 결제 필요액 (0이면 예치금으로 완납)
	Long paymentId,             // PG 결제 필요 시 Payment ID (없으면 null)
	String pgOrderId,           // PG 결제 필요 시 PG Order ID (없으면 null)
	boolean completed           // 결제 완료 여부 (예치금으로 완납 시 true)
) {

	/**
	 * 예치금으로 완납된 경우
	 */
	public static PaymentInitiateResult completedWithWallet(Long orderId, Money walletUsed, Long paymentId) {
		return new PaymentInitiateResult(orderId, walletUsed, Money.zero(), paymentId, null, true);
	}

	/**
	 * PG 추가 결제가 필요한 경우
	 */
	public static PaymentInitiateResult requiresPgPayment(
		Long orderId, Money walletUsed, Money pgRequired, Long paymentId, String pgOrderId
	) {
		return new PaymentInitiateResult(orderId, walletUsed, pgRequired, paymentId, pgOrderId, false);
	}
}
