package payment.usecase.result;

import app.giftify.shared.domain.vo.Money;

/**
 * 펀딩 복합 결제 결과.
 * 예치금 사용액, PG 결제 필요액, 완료 여부 등을 포함합니다.
 */
public record FundingContributeResult(
	Money walletUsed,           // 예치금 사용액
	Money pgPaymentRequired,    // PG 결제 필요액 (0이면 예치금으로 완납)
	Long paymentId,             // PG 결제 필요 시 Payment ID (없으면 null)
	String orderId,             // PG 결제 필요 시 Order ID (없으면 null)
	boolean completed           // 결제 완료 여부 (예치금으로 완납 시 true)
) {

	/**
	 * 예치금으로 완납된 경우
	 */
	public static FundingContributeResult completedWithWallet(Money walletUsed) {
		return new FundingContributeResult(walletUsed, Money.zero(), null, null, true);
	}

	/**
	 * PG 추가 결제가 필요한 경우
	 */
	public static FundingContributeResult requiresPgPayment(
		Money walletUsed, Money pgRequired, Long paymentId, String orderId
	) {
		return new FundingContributeResult(walletUsed, pgRequired, paymentId, orderId, false);
	}
}
