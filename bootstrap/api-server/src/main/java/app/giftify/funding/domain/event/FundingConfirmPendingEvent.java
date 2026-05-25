package app.giftify.funding.domain.event;

import app.giftify.support.common.event.BaseDomainEvent;

public class FundingConfirmPendingEvent extends BaseDomainEvent {
    private final Long fundingId;
    private final Long productId;

    public FundingConfirmPendingEvent(Long fundingId, Long productId) {
        super();
        this.fundingId = fundingId;
        this.productId = productId;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public Long getProductId() {
        return productId;
    }
}
