package app.giftify.order.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;

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
