package app.giftify.wallet.domain.event;

import java.time.LocalDateTime;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

/**
 * 지갑 충전 완료 이벤트.
 * 지갑 잔액이 충전되었을 때 발행됩니다.
 */
public final class WalletChargedEvent extends BaseDomainEvent {
	private final Long walletId;
	private final Long memberId;
	private final String chargeOrderId;
	private final Money amount;
	private final Money balanceAfter;
	private final LocalDateTime chargedAt;

	public WalletChargedEvent(
		Long walletId,
		Long memberId,
		String chargeOrderId,
		Money amount,
		Money balanceAfter,
		LocalDateTime chargedAt
	) {
		super();
		this.walletId = walletId;
		this.memberId = memberId;
		this.chargeOrderId = chargeOrderId;
		this.amount = amount;
		this.balanceAfter = balanceAfter;
		this.chargedAt = chargedAt;
	}

	public Long getWalletId() {
		return walletId;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getChargeOrderId() {
		return chargeOrderId;
	}

	public Money getAmount() {
		return amount;
	}

	public Money getBalanceAfter() {
		return balanceAfter;
	}

	public LocalDateTime getChargedAt() {
		return chargedAt;
	}
}
