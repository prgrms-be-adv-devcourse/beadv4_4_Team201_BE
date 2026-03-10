package app.giftify.shared.domain.event.order;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class OrderConfirmedEvent extends BaseDomainEvent {
    private final Long fundingId;

    public OrderConfirmedEvent(Long fundingId) {
        super();
        this.fundingId = fundingId;
    }

    public Long getFundingId() {
        return fundingId;
    }
}
