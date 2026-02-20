package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class PaymentFailedCancelEvent extends BaseDomainEvent {
    private final Long orderId;

    public PaymentFailedCancelEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
