package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.vo.Money;

public record PaymentCreatedResult(
	Long paymentId,
	String idempotencyKey,
	PaymentStatus status,
	boolean requiresPgApproval,
	WalletInfo walletInfo
) {
	public PaymentCreatedResult(Long paymentId, String idempotencyKey, PaymentStatus status, boolean requiresPgApproval) {
		this(paymentId, idempotencyKey, status, requiresPgApproval, null);
	}

	public static PaymentCreatedResult insufficientWalletBalance(
		Long paymentId,
		String idempotencyKey,
		Money requiredAmount,
		Money currentBalance
	) {
		return new PaymentCreatedResult(
			paymentId,
			idempotencyKey,
			PaymentStatus.PENDING,
			false,
			new WalletInfo(requiredAmount, currentBalance)
		);
	}

	public boolean hasInsufficientBalance() {
		return walletInfo != null;
	}

	public record WalletInfo(Money requiredAmount, Money currentBalance) {
		public Money shortfall() {
			return requiredAmount.minus(currentBalance);
		}
	}
}
