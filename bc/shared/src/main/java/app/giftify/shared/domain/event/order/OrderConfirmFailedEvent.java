package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class OrderConfirmFailedEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final String reason;

    public OrderConfirmFailedEvent(Long fundingId, String reason) {
        super();
        this.fundingId = fundingId;
        this.reason = reason;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public String getReason() {
        return reason;
    }
}
