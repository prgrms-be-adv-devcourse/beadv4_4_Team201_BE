package app.giftify.shared.domain.event.wallet;

import org.springframework.modulith.events.Externalized;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

@Externalized("wallet.settlement-completed::#{#this.getSettlementId()}")
public class WalletSettlementCompletedEvent extends BaseDomainEvent {
	private final Long settlementId;
	private final Long sellerId;
	private final Money totalAmount;

	public WalletSettlementCompletedEvent(Long settlementId, Long sellerId, Money totalAmount) {
		super();
		this.settlementId = settlementId;
		this.sellerId = sellerId;
		this.totalAmount = totalAmount;
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

}
