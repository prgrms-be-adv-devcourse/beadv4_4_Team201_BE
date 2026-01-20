package app.giftify.shared.domain.event.settlement;

import app.giftify.shared.domain.event.BaseDomainEvent;
import app.giftify.shared.domain.vo.Money;

public class SettlementFailedEvent extends BaseDomainEvent {
	private final Long settlementId;
	private final Long sellerId;
	private final Money totalAmount;
	private final String reason;
	private final int retryCount; // 다음 Retry마다 count 증가 시켜서 새로 발행

	public SettlementFailedEvent(Long settlementId, Long sellerId, Money totalAmount, String reason, int retryCount) {
		super();
		this.settlementId = settlementId;
		this.sellerId = sellerId;
		this.totalAmount = totalAmount;
		this.reason = reason;
		this.retryCount = retryCount;
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

	public int getRetryCount() {
		return retryCount;
	}
}
