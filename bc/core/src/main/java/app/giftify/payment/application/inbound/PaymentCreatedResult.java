package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.vo.Money;

public record PaymentCreatedResult(
	Long paymentId,
	String orderId,           // 프론트엔드 Toss SDK 호출 시 필수!!
	String idempotencyKey,
	PaymentStatus status,
	boolean requiresPgApproval,
	WalletInfo walletInfo
) {
	// walletInfo 없이 생성
	public PaymentCreatedResult(Long paymentId, String orderId, String idempotencyKey, PaymentStatus status, boolean requiresPgApproval) {
		this(paymentId, orderId, idempotencyKey, status, requiresPgApproval, null);
	}

	// 잔액 부족 시 사용
	public static PaymentCreatedResult insufficientWalletBalance(
		Long paymentId,
		String orderId,           // 추가
		String idempotencyKey,
		Money requiredAmount,
		Money currentBalance
	) {
		return new PaymentCreatedResult(
			paymentId,
			orderId,                  // 추가
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
