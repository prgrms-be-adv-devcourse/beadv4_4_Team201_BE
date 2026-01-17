package app.giftify.shared.domain.event.settlement;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

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
