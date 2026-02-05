package app.giftify.payment.application.inbound;

import app.giftify.payment.domain.PaymentStatus;
import app.giftify.shared.domain.vo.Money;

/**
 * 결제 생성 결과.
 *
 * <p>{@code orderId}는 Toss SDK 호출 시 필요하며, 멱등성 키 역할도 겸합니다.</p>
 */
public record PaymentCreatedResult(
	Long paymentId,
	String orderId,           // 프론트엔드 Toss SDK 호출 시 필수 + 멱등성 키
	PaymentStatus status,
	boolean requiresPgApproval,
	WalletInfo walletInfo
) {
	// walletInfo 없이 생성
	public PaymentCreatedResult(Long paymentId, String orderId, PaymentStatus status, boolean requiresPgApproval) {
		this(paymentId, orderId, status, requiresPgApproval, null);
	}

	// 잔액 부족 시 사용
	public static PaymentCreatedResult insufficientWalletBalance(
		Long paymentId,
		String orderId,
		Money requiredAmount,
		Money currentBalance
	) {
		return new PaymentCreatedResult(
			paymentId,
			orderId,
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
