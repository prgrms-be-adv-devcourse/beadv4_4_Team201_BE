package app.giftify.settlement.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;
import app.giftify.support.common.money.Money;

public class SettlementCompletedEvent extends BaseDomainEvent {
	private final Long settlementId;
	private final Long sellerId;
	private final Long sellerWalletId;
	private final Money totalAmount;

	public SettlementCompletedEvent(Long settlementId, Long sellerId, Long sellerWalletId, Money totalAmount) {
		super();
		this.settlementId = settlementId;
		this.sellerId = sellerId;
		this.sellerWalletId = sellerWalletId;
		this.totalAmount = totalAmount;
	}

	public Long getSettlementId() {
		return settlementId;
	}

	public Long getSellerId() {
		return sellerId;
	}

	public Long getSellerWalletId() {
		return sellerWalletId;
	}

	public Money getTotalAmount() {
		return totalAmount;
	}
}
