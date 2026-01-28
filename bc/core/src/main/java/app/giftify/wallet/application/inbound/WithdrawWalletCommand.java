package app.giftify.wallet.application.inbound;

import app.giftify.shared.domain.vo.Money;

/**
 * 지갑 출금 요청 커맨드
 */
public record WithdrawWalletCommand(
	Long memberId,
	Money amount,
	String bankCode,
	String accountNumber
) {
}
