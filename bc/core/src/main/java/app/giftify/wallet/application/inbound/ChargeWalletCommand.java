package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

/**
 * Wallet 충전 Command.
 *
 * @param memberId 회원 ID
 * @param amount 충전 금액
 * @param paymentKey Toss PG에서 받은 결제 키
 * @param chargeOrderId 충전 전용 주문 ID (예: "CHARGE-{memberId}-{timestamp}")
 */
public record ChargeWalletCommand(
	Long memberId,
	Money amount,
	String paymentKey,
	String chargeOrderId
) {
}
