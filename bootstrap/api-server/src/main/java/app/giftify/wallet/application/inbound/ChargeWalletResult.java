package app.giftify.wallet.application.inbound;

import app.giftify.support.common.money.Money;

/**
 * Wallet 충전 결과.
 *
 * @param walletId Wallet ID
 * @param memberId 회원 ID
 * @param balanceAfter 충전 후 잔액
 * @param chargedAmount 충전된 금액
 * @param transactionId 거래 식별자
 */
public record ChargeWalletResult(
	Long walletId,
	Long memberId,
	Money balanceAfter,
	Money chargedAmount,
	String transactionId
) {
}
