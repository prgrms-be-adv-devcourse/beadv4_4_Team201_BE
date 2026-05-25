package app.giftify.wallet.domain.event;

import java.time.LocalDateTime;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.support.common.money.Money;

/**
 * 지갑 결제 차감 완료 이벤트.
 * Payment Aggregate가 수신하여 결제 완료 처리를 진행합니다.
 */
public final class WalletDeductedEvent extends BaseDomainEvent {
	private final Long walletId;
	private final Long memberId;
	private final Long paymentId;
	private final String orderId;
	private final Money amount;
	private final LocalDateTime deductedAt;

	public WalletDeductedEvent(
		Long walletId,
		Long memberId,
		Long paymentId,
		String orderId,
		Money amount,
		LocalDateTime deductedAt
	) {
		super();
		this.walletId = walletId;
		this.memberId = memberId;
		this.paymentId = paymentId;
		this.orderId = orderId;
		this.amount = amount;
		this.deductedAt = deductedAt;
	}

	public Long getWalletId() {
		return walletId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Long getPaymentId() {
		return paymentId;
	}

	public String getOrderId() {
		return orderId;
	}

	public Money getAmount() {
		return amount;
	}

	public LocalDateTime getDeductedAt() {
		return deductedAt;
	}
}
