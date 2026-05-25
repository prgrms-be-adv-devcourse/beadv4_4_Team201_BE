package app.giftify.wallet.application.inbound;

import app.giftify.support.common.money.Money;

/**
 * Wallet 충전 Command.
 *
 * @param memberId 회원 ID
 * @param amount 충전 금액
 * @param chargeOrderId 충전 전용 주문 ID (예: "CHARGE-{memberId}-{timestamp}")
 */
public record ChargeWalletCommand(
	Long memberId,
	Money amount,
	String chargeOrderId
) {
}
