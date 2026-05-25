package app.giftify.settlement.domain.event;

import org.springframework.modulith.events.Externalized;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.support.common.money.Money;

@Externalized("settlement.created::#{#this.getSettlementId()}")
public class SettlementCreatedEvent extends BaseDomainEvent {
	private final Long settlementId;
	private final Long sellerId;
	private final Money totalAmount;

	public SettlementCreatedEvent(Long settlementId, Long sellerId, Money totalAmount) {
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
