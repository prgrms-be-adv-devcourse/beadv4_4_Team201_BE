package app.giftify.shared.domain.event.wallet;

import org.springframework.modulith.events.Externalized;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

@Externalized("wallet.settlement-failed::#{#this.getSettlementId()}")
public class WalletSettlementFailedEvent extends BaseDomainEvent {
	private final Long settlementId;
	private final Long sellerId;
	private final Money totalAmount;
	private final String reason;

	public WalletSettlementFailedEvent(Long settlementId, Long sellerId, Money totalAmount, String reason) {
		super();
		this.settlementId = settlementId;
		this.sellerId = sellerId;
		this.totalAmount = totalAmount;
		this.reason = reason;
	}

	public Long getSettlementId() {
		return settlementId;
	}

	public Long getSellerId() {
		return sellerId;
	}

	public Money getTotalAmount() {
		return totalAmount;
	}

	public String getReason() {
		return reason;
	}
}
