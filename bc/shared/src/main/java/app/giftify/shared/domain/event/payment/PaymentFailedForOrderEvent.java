package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class PaymentFailedForOrderEvent extends BaseDomainEvent {
    private final Long orderId;

    public PaymentFailedForOrderEvent(Long orderId) {
        super();
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "PaymentFailedForOrderEvent{" +
                "orderId=" + orderId +
                ", eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}