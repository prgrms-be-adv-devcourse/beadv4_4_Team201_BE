package app.giftify.shared.domain.event.payment;

import app.giftify.shared.domain.event.BaseDomainEvent;

public class PaymentCancelledForOrderEvent extends BaseDomainEvent {
    private final Long orderId;

    public PaymentCancelledForOrderEvent(Long orderId) {
        super();
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    @Override
    public String toString() {
        return "PaymentCancelledForOrderEvent{" +
                "orderId=" + orderId +
                ", eventId='" + getEventId() + '\'' +
                ", occurredAt=" + getOccurredAt() +
                '}';
    }
}